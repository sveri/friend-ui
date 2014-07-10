# Warning
This project is not used in a productive environment yet, so try it on your own risk.

# friendui

A Clojure library designed to wrap cemericks friend ([_https://github.com/cemerick/friend_]) library.
It provides templates for login / signup with email activation. Additionally there is an admin interface where one can
edit existing users or add new ones.

## Dependencies
* enlive (v1.1.5) as templating library. 
* friend (0.2.1) 
* Bootstrap (> 3.0) is required too if you want it to look nice.

## "Installation"

Friendui is available in Clojars:
[![Clojars Project](http://clojars.org/de.sveri/friendui/latest-version.svg)](http://clojars.org/de.sveri/friendui)

## Usage

### Config
Friendui looks for a configuration file named: friendui-config.edn in the classpath.
It should look like this:

```clojure
{
  :username-kw :user/email
  :pw-kw :user/password
  :activated-kw :user/activated
  :role-kw :user/role
  :hostname "http://yourhost.com/" ;used for generation of activation link
  :mail-from "activation@yourhost.com" ;used as from mail address
  :base-template-content-key :content ; the key where the base template expects the page content
  :base-template-title-key :title ; the key where the base template expects the page title
  :available-roles [:user/admin :user/free]
  :new-user-role :user/free
  :user-signup-redirect "/user/accountcreated" ; default uri - is provided by friendui
  :account-activated-redirect "/user/accountactivated" ; default uri - is provided by friendui   
  :send-activation-email true ; email on activation can be turned off to fit your needs
  :activation-mail-subject "Please activate your account."
  }
```

Then you have to alter the root binding of the base template var like this:

```clojure
(:require [de.sveri.friendui.globals :as f-global])

(html/deftemplate base (str template-path "base.html")
                  [{:keys [title main]}]
                  [:#title] (util/maybe-content title)      ; this corresponds to the :base-template-title-key key in the config
                  [:#content] (util/maybe-substitute main)) ; this corresponds to the :base-template-content-key key in the config

(alter-var-root #'f-global/base-template (fn [_] (partial base)))
```

### E-Mail

If you want to send an E-Mail on activation which the user can use to activate it's account sendmail is required on your 
localhost with the SENDMAIL variable set.
Currently there is no other option available for sending mail. Of course you can send an E-Mail from within your application,
as the activationid is provided in the storage protocol.

### Protocol
And finally you have to implement a protocol to retrieve and store user data:

```clojure
(defprotocol FrienduiStorage
  "Defines methods to acces user storage for retrieval and update."
  (account-activated? [this activationid]
                      "Provides an id. Expects a boolean return value indicating if the user, belonging to the id is
                      activated or not.")
  (activate-account [this activationid]
                    "Should set the user with the given id to activated. After this function was called successfully
                    (account-activated?) should return true.")
  (create-user [this email password role activationid]
               "Should add a new user to your data store. Return value is not checked.")
  (get-all-users [this]
                 "Called from the admin view. Expects a list of all known users in this format:
                 ({:user/activated false, :user/role :user/admin, :user/email unique-email@host.com}
                 ...)
                 Where each key corresponds to the configured keyword from friendui-config.edn")
  (get-user-for-activation-id [this id]
                              "Should return a map containing the username and role of this user like this:
                              {:username username :roles #{role}}")
  (update-user [this username data-map]
               "Updates the user with the given data map of the form: {:user/activated boolean :user/role :user/free}")
  (username-exists? [this username]
                    "Expects true if the username exists already in the storage, false otherwise."))
```

Then you pass this storage to the friendui routes like this:
```clojure
(:require [de.sveri.friendui.routes.user :refer [friend-routes]])

(defroutes allroutes
    (friend-routes FrienduiStorageImpl)
    ...)
    
(def app
  (handler/site
    (friend/authenticate allroutes friend-settings)))
```

### Your friend settings
Of course you have to define your friend settings yourself in your application, this is an example of mine:
```clojure
(def friend-settings
  {:credential-fn             (partial creds/bcrypt-credential-fn user/login-user)
   :workflows                 [(workflows/interactive-form)]
   :login-uri                 "/user/login"
   :unauthorized-redirect-uri "/user/login"
   :default-landing-uri       "/"})
```

This should get you up and running.

## Provided Resources:

```clojure
(GET "/user/login") ;expects a "username" / "password" combination
(GET "/user/signup")
(POST "/user/signup") ;expects email / password / confirm parameters
(GET "/user/accountcreated")
(GET "/user/activate/:id")
(GET "/user/accountactivated")
(GET "/user/admin" [filter])
(POST "/user/update" [username role active])
(POST "/user/add" [email password confirm]) ; used in admin view
(ANY "/user/logout")
```

## Screenshots

### Signup
![Alt Signup](/docs/signup.jpg "Signup")

### Signup Error
![Alt Signup Error](/docs/signup_error.jpg "Signup Error")

### Account Created
![Alt Account Created](/docs/account_created.jpg "Account Created")

### Admin View
![Alt Admin View](/docs/admin_view.jpg "Admin View")

## Version History
**0.3.2** Added default unauthorized handler and an example storage protocol implementation at: https://github.com/sveri/friendui-datomic
**0.3.1** Bugfix and documentation release

**0.3.0** decoupled from datomic which caused a lot of API changes.

**0.2.4** - Broken build - don't use it
Added Administrator interface for users. User roles and activation status can be updated by administrators.
New Users can be added by administrators.
A filter is available for the user list

**0.2.3** First working release with an implementation that depends on enlive and datomic.

## License

Distributed under the Eclipse Public License either version 1.0 or any later version.
