/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.myfridget.server.ejb;

import com.myfridget.server.db.entity.User;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import javax.annotation.Resource;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Stateless
public class UsersEJB {

    @PersistenceContext(unitName = "Fridget_EJBsPU")
    private EntityManager em;
    
    @Resource
    private SessionContext ctx;
	
    @RolesAllowed({"superuser"})
    public List<User> getUsers() {
        return em.createNamedQuery("User.findAll", User.class).getResultList();
    }
    
    @PermitAll
    public User getUser(int userId) {
        return em.find(User.class, userId);
    }
    
    @RolesAllowed({"superuser"})
    public void saveUser(User user) {
        if (user.getId() == null) {
            // new user, set a default password
            setDefaultPassword(user);
            em.persist(user);
        } else {
            // existing user, merge back
            em.merge(user);
        }
    }

    @RolesAllowed({"superuser"})
    public void deleteUser(int userId) {
        em.remove(em.find(User.class, userId));
    }

    @RolesAllowed({"superuser"})
    public void resetPassword(int userId) {
        setDefaultPassword(em.find(User.class, userId));
    }

    @RolesAllowed({"user","admin","superuser"})
    public User getCurrentUser() {
        return em.createNamedQuery("User.findByEmail", User.class).setParameter("email", ctx.getCallerPrincipal().getName()).getSingleResult();
    }

    @RolesAllowed({"user","admin","superuser"})
    public void changePassword(String oldPassword, String newPassword) {
        User user = getCurrentUser();
        if (!md5Hex(oldPassword).equals(user.getPassword()))
            throw new RuntimeException("Sorry, you entered the wrong password.");
        if (oldPassword.equals(newPassword))
            throw new RuntimeException("Sorry, the new password cannot be the same as the old password.");
        user.setPassword(md5Hex(newPassword));
        //user.setPwStatus((byte)1);
    }
    
    protected static void setDefaultPassword(User user) {
        // set a default password and pwStatus = 0 (force change pw)
        //user.setPwStatus((byte)0); // change password
        user.setPassword(md5Hex("fridget")); // XXX default password
    }
    
    protected static String md5Hex(String in) {
        StringBuilder md5 = new StringBuilder(new BigInteger(1, md5(in)).toString(16));
        while (md5.length()<32) md5.insert(0, '0');
        return md5.toString();
    }
    
    protected static byte[] md5(String in) {
        try {
            return MessageDigest.getInstance("MD5").digest(in.getBytes("ISO-8859-1"));
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
        }
    }
}
