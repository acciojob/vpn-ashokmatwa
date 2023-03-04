package com.driver.model;

import javax.persistence.*;

@Entity
@Table(name = "connection")
public class Connection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    public Connection() {
    }

    //serviceP --> connection
    @ManyToOne
    @JoinColumn
    private ServiceProvider serviceProvider;

    //user --> connection
    @ManyToOne
    @JoinColumn
    private User user;

    public ServiceProvider getServiceProvider() {
        return serviceProvider;
    }

    public void setServiceProvider(ServiceProvider serviceProvider) {
        this.serviceProvider = serviceProvider;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
