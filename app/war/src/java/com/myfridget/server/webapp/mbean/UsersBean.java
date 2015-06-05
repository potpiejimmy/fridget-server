package com.myfridget.server.webapp.mbean;

import com.myfridget.server.db.entity.User;
import com.myfridget.server.ejb.UsersEJB;
import com.myfridget.server.webapp.util.WebUtils;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import javax.ejb.EJB;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.servlet.ServletException;

@ManagedBean
@SessionScoped
public class UsersBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@EJB
	private UsersEJB ejb;
	
	private User current = new User();

	public void clear() {
		current = new User();
	}

	public User getCurrent() {
		return current;
	}
	
	public List<User> getUsers() {
		return ejb.getUsers();
	}
	
	public void save() {
		ejb.saveUser(current);
                clear();
	}
	
	public void edit(User u) {
		current = u;
	}
	
	public void delete(User u) {
		try {
			ejb.deleteUser(u.getId());
		} catch (Exception ex) {
			WebUtils.addFacesMessage(ex);
		}
	}

        public void resetPassword(User u) {
            ejb.resetPassword(u.getId());
	}
        
        public void logout() {
            try {
                WebUtils.getHttpServletRequest().logout();
                FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
                WebUtils.redirect("/");
            } catch (ServletException e) {
            } catch (IOException e) {
        }
    }
}
