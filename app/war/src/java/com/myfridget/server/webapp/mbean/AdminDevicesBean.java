package com.myfridget.server.webapp.mbean;

import com.myfridget.server.db.entity.AdDevice;
import com.myfridget.server.db.entity.User;
import com.myfridget.server.ejb.AdDeviceEJB;
import com.myfridget.server.ejb.UsersEJB;
import com.myfridget.server.util.EPDUtils;
import com.myfridget.server.webapp.util.WebUtils;
import java.io.Serializable;
import java.util.List;

import javax.ejb.EJB;

import java.util.ArrayList;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.model.SelectItem;
import org.primefaces.model.DualListModel;

@ManagedBean
@SessionScoped
public class AdminDevicesBean implements Serializable, Converter {

	private static final long serialVersionUID = 1L;
        
        protected final static int DEVICE_TYPE_CORE   = 0x10; // use lower nibble for image type
        protected final static int DEVICE_TYPE_PHOTON = 0x20; // use lower nibble for image type

        @EJB
        private AdDeviceEJB deviceEjb;
        @EJB
        private UsersEJB usersEjb;
	
	private AdDevice currentDevice = null;

        private DualListModel<User> assignedUsers = new DualListModel<>();
    
        public List<SelectItem> getDeviceTypesSelectItems() {
            List<SelectItem> result = new ArrayList<>();
            result.add(new SelectItem(DEVICE_TYPE_CORE   + EPDUtils.SPECTRA_DISPLAY_TYPE_441, "Spark Core with Spectra 4.41\" (400x300)"));
            result.add(new SelectItem(DEVICE_TYPE_CORE   + EPDUtils.SPECTRA_DISPLAY_TYPE_74, "Spark Core with Spectra 7.4\" (480x800)"));
            result.add(new SelectItem(DEVICE_TYPE_PHOTON + EPDUtils.SPECTRA_DISPLAY_TYPE_441, "Particle Photon with Spectra 4.41\" (400x300)"));
            result.add(new SelectItem(DEVICE_TYPE_PHOTON + EPDUtils.SPECTRA_DISPLAY_TYPE_74, "Particle Photon with Spectra 7.4\" (480x800)"));
            return result;
        }
    
	public void clear() {
                newDevice();
	}
        
        public void newDevice() {
		currentDevice = new AdDevice();
                assignedUsers.setSource(usersEjb.getUsers());
                assignedUsers.setTarget(new ArrayList<User>());
        }

	public AdDevice getCurrentDevice() {
		return currentDevice;
	}
	
	public List<AdDevice> getDevices() {
		return deviceEjb.getAllDevices();
	}
	
        public DualListModel<User> getAssignedUsers() {
                if (currentDevice == null) newDevice();
                return assignedUsers;
        }

        public void setAssignedUsers(DualListModel<User> assignedUsers) {
                this.assignedUsers = assignedUsers;
        }

        public String getAssignedUsersAsString(AdDevice device) {
            StringBuilder stb = new StringBuilder();
            int i = 0;
            for (User user : deviceEjb.getAssignedUsers(device.getId())) {
                if (i > 0) {
                    stb.append(", ");
                    if (i%5 == 0) stb.append("<br/>");
                }
                stb.append(user.getEmail().replaceAll("<", "&lt;").replaceAll(">", "&gt;"));
                i++;
            }
            return stb.toString();
        }
        
	public void save() {
            deviceEjb.saveDevice(currentDevice, assignedUsers.getTarget());
            clear();
	}
	
	public void edit(AdDevice p) {
            currentDevice = p;

            List<User> source = new ArrayList<User>(usersEjb.getUsers());
            List<User> target = deviceEjb.getAssignedUsers(currentDevice.getId());
            source.removeAll(target);
            assignedUsers.setSource(source);
            assignedUsers.setTarget(target);
	}
	
	public void delete(AdDevice p) {
            try {
//                deviceEjb.deleteDevice(p.getId());
                clear();
            } catch (Exception ex) {
                WebUtils.addFacesMessage(ex);
            }
	}

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        return usersEjb.getUser(Integer.parseInt(value));
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        return ((User)value).getId().toString();
    }
}
