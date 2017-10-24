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

## References

* [Spring Security ACL reference](https://docs.spring.io/autorepo/docs/spring-security/current/reference/htmlsingle/#domain-acls-key-concepts)