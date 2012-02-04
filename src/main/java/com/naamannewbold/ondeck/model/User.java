package com.naamannewbold.ondeck.model;

import javax.persistence.*;

/**
 * TODO: Javadoc
 *
 * @author Naaman Newbold
 */
@Entity
@Table(name = "OndeckUser")
public class User {
    private String id;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
