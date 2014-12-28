package com.myfridget.server.webapp.mbean;

import com.myfridget.server.db.entity.AdDevice;
import com.myfridget.server.db.entity.User;
import com.myfridget.server.ejb.AdDeviceEJBLocal;
import com.myfridget.server.ejb.UsersEJBLocal;
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
import org.primefaces.model.DualListModel;

@ManagedBean
@SessionScoped
public class AdminDevicesBean implements Serializable, Converter {

	private static final long serialVersionUID = 1L;

        @EJB
        private AdDeviceEJBLocal deviceEjb;
        @EJB
        private UsersEJBLocal usersEjb;
	
	private AdDevice currentDevice = null;

        private DualListModel<User> assignedUsers = new DualListModel<User>();
    
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
