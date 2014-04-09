# Warning
This project is not used in a productive environment yet, so try it on your own risk.

# friendui

A Clojure library designed to wrap cemericks friend ([_https://github.com/cemerick/friend_]) library.
It provideh templates for login / signup with email activation.
As backend it uses datomic and enlive as templating library.

## "Installation"

Friendui is available in Clojars. Add this `:dependency` to your Leiningen
`project.clj`:

```clojure
[com.cemerick/friendui "0.2.0"]
```

Or, add this to your Maven project's `pom.xml`:

```xml
<repository>
  <id>clojars</id>
  <url>http://clojars.org/repo</url>
</repository>

<dependency>
  <groupId>de.sveri</groupId>
  <artifactId>friendui</artifactId>
  <version>0.2.0</version>
</dependency>
```

## Usage

First you have to setup datomic. A sample schema is provided in [_https://github.com/sveri/friend-ui/blob/master/resources/schema/datomic-schema.edn_].

Next a configuration file is needed in the classpath. The name has to be: friendui-config.edn.
It could look like this:

```clojure
{
  :datomic-uri "datomic:dev://localhost:4334/alias"
  :partition-id :your-id
  :username-kw :user/email
  :pw-kw :user/password
  :activated-kw :user/activated
  :role-kw :user/role
  :hostname "localhost.de" ;used for generation of activation link
  :mail-from "from@from" ;used as from mail address
  }
```

The last thing to do is alter the root binding of the base template var like this:

```clojure
(html/deftemplate base (str template-path "base.html")
                  [{:keys [title main]}]
                  [:#title] (util/maybe-content title)
                  [:#content] (util/maybe-substitute main)
                  )

(alter-var-root #'user-global/base-template (fn [f] (partial base)))
```

This should get you up and running.

## Provided Resources:

```clojure
(GET "/user/login") ;expects a "username" / "password" combination
(GET "/user/signup")
(POST "/user/signup")
(GET "/user/accountcreated")
(GET "/user/activate/:id")
(GET "/user/accountactivated")
(friend/logout (ANY "/user/logout")
```

## License

Distributed under the Eclipse Public License either version 1.0 or any later version.
