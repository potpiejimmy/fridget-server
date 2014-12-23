/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.myfridget.server.ejb;

import com.myfridget.server.db.entity.User;
import java.util.List;
import javax.ejb.Local;

/**
 *
 */
@Local
public interface UsersEJBLocal {

    /**
     * Returns the list of users
     * @return list of users
     */
    public List<User> getUsers();

    /**
     * Get the current user
     * @return current user
     */
    public User getCurrentUser();
    
    /**
     * Saves the user to the database.
     * @param user a user value object
     */
    public void saveUser(User user);

    /**
     * Deletes the user
     * @param userId a user ID
     */
    public void deleteUser(int userId);

    /**
     * Resets the password for the given user to
     * a default value.
     * @param userId a user ID
     */
    public void resetPassword(int userId);
    
    /**
     * Changes the password of the currently logged in user
     * @param oldPassword old password
     * @param newPassword new password
     */
    public void changePassword(String oldPassword, String newPassword);
}
