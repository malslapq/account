package com.zero.account.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Table;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@EntityListeners(EntityListeners.class)
@Table(name = "account_user")
public class AccountUser extends BaseEntity {

    private String name;

}