# Spring Security ACL
## Technology
* Java8
* Maven3
* SpringBoot 1.5.8-RELEASE

## Domain Object Security & Access Control Lists
### Domain Object or granular access control
* Users control some objects (`possessions`). And each user has the right to work only with his own possession objects
* Secondly another user may need to use a possesion from another user (`borrow`)
* In above case, the `owner` will grant some privileges to the `borrower` over that particular possession
* Authorization model used until now is not flexible enough to handle above scenario because it define authorities per type of object. 
  So it can define that users that have `this authority` can do `this action` on `ALL objects` of this `type`. But can't define that
  users that have `this authority` can do `this action` on `object A` of this `type`, but not on `object B of the SAME type`.
* Granular access can be done in a manually way combining per-type authorization and custom business logic with extra checks but is not a good way because is difficult
* More flexible and more granular mode is using Spring Security new model that allow us to define the security semantics of a specific domain object, not just a class/type of objects.
* This model it’s a generic way of defining authorization semantics for each domain entity using what is called an access control list - ACL. 
  And it's going to allow us full granular control over exactly which users in the system can access exactly which objects.

### Access Control Lists
#### Structure (is managed in db)
* `Security Identity (SID)`: represent the principal that gets access to the domain object. The SID can also represent an authority
* `Domain Object`: is composed of two entities
    * `Class`: the java class of the entity
    * `Object Identity`: the main identifier of the entity tryig to secure
* `ACL Entry`: represents the actual permissions that the principal has on the domain objects. By default these are: read, write, create, delete, admin - and they’re represented with an integer bit mask
    * `32 bits mask`: 5 bits for above permissions and other and rest can be used for custom types 

```json
{
  "SID" : "Mauro",
  "Domain Object": {
    "Class": "Possession",
    "ObjectId": "Car"
  },
  "Entry": "W (own)"
}

{
  "SID" : "John",
  "Domain Object": {
    "Class": "Possession",
    "ObjectId": "Car"
  },
  "Entry": "R (borrow)"
}
```
#### Database structure
* MySQL version schema ([others acl schemas](https://docs.spring.io/autorepo/docs/spring-security/current/reference/htmlsingle/#dbschema-acl))
```sql
CREATE TABLE IF NOT EXISTS acl_sid (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    principal BOOLEAN NOT NULL,
    sid VARCHAR(100) NOT NULL,
    UNIQUE KEY unique_acl_sid (sid, principal)
) ENGINE=InnoDB;
--
CREATE TABLE IF NOT EXISTS acl_class (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    class VARCHAR(100) NOT NULL,
    UNIQUE KEY uk_acl_class (class)
) ENGINE=InnoDB;
--
CREATE TABLE IF NOT EXISTS acl_object_identity (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    object_id_class BIGINT UNSIGNED NOT NULL,
    object_id_identity BIGINT NOT NULL,
    parent_object BIGINT UNSIGNED,
    owner_sid BIGINT UNSIGNED,
    entries_inheriting BOOLEAN NOT NULL,
    UNIQUE KEY uk_acl_object_identity (object_id_class, object_id_identity),
    CONSTRAINT fk_acl_object_identity_parent FOREIGN KEY (parent_object) REFERENCES acl_object_identity (id),
    CONSTRAINT fk_acl_object_identity_class FOREIGN KEY (object_id_class) REFERENCES acl_class (id),
    CONSTRAINT fk_acl_object_identity_owner FOREIGN KEY (owner_sid) REFERENCES acl_sid (id)
) ENGINE=InnoDB;
--
CREATE TABLE IF NOT EXISTS acl_entry (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    acl_object_identity BIGINT UNSIGNED NOT NULL,
    ace_order INTEGER NOT NULL,
    sid BIGINT UNSIGNED NOT NULL,
    mask INTEGER UNSIGNED NOT NULL,
    granting BOOLEAN NOT NULL,
    audit_success BOOLEAN NOT NULL,
    audit_failure BOOLEAN NOT NULL,
    UNIQUE KEY unique_acl_entry (acl_object_identity, ace_order),
    CONSTRAINT fk_acl_entry_object FOREIGN KEY (acl_object_identity) REFERENCES acl_object_identity (id),
    CONSTRAINT fk_acl_entry_acl FOREIGN KEY (sid) REFERENCES acl_sid (id)
) ENGINE=InnoDB;
```

* Example setup
1. Domain objects entities
```java
@Entity
@PasswordMatches
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Email
    @NotEmpty(message = "Email is required.")
    private String email;

    @ValidPassword
    @NotEmpty(message = "Password is required.")
    private String password;

    @Transient
    @NotEmpty(message = "Password confirmation is required.")
    private String passwordConfirmation;

    public User() {
        super();
    }
    
    // setters and getters
}
```

```java
@Entity
public class Possession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    //

    public Possession() {
        super();
    }

    public Possession(String name) {
        super();
        this.name = name;
    }
    
    // getters and setters
}
```
2. Artifacts of configurations
```sql
INSERT INTO acl_sid (id, principal, sid) 
VALUES
(1, 1, 'user1'),
(2, 1, 'user2');
-- 
INSERT INTO acl_class (id, class) 
VALUES
(1, 'com.maurofokker.demo.model.Possession');
--
INSERT INTO acl_object_identity 
(id, object_id_class, object_id_identity, parent_object, owner_sid, entries_inheriting) 
VALUES
(1, 1, 1, NULL, 1, 1), -- user1 Possession object identity
(2, 1, 2, NULL, 1, 1), -- Common Possession object identity
(3, 1, 3, NULL, 1, 1); -- user2 Possession object identity
```

3. Entry configurations
```sql
-- each has access to their own possessions, and shared posession is shared to both users to access
INSERT INTO acl_entry 
(id, acl_object_identity, ace_order, sid, mask, granting, audit_success, audit_failure) 
VALUES
(1, 1, 0, 1, 16, 1, 0, 0), -- user1 has Admin permission for Possession 1
(2, 2, 0, 1, 16, 1, 0, 0), -- user1 has Admin permission for Common Possession 2
(3, 2, 1, 2, 1, 1, 0, 0),  -- user2 has Read permission for Common Possession 2
(4, 3, 0, 2, 16, 1, 0, 0); -- user2 has Admin permission for Eric Possession 3
```
## References

* [Spring Security ACL reference](https://docs.spring.io/autorepo/docs/spring-security/current/reference/htmlsingle/#domain-acls-key-concepts)
* [Spring Security ACL schemas](https://docs.spring.io/autorepo/docs/spring-security/current/reference/htmlsingle/#dbschema-acl)