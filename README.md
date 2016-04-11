<p align="center">
  <img src="https://raw.githubusercontent.com/blocks4j/reconf-jvm/master/other/images/reconf.png"/>
</p>

[![Build Status](https://travis-ci.org/reconf/reconf-jvm.svg?branch=master)](https://travis-ci.org/blocks4j/reconf-jvm)
# ReConf JVM in 30 seconds

The ReConf JVM Project is a library that provides an easy way to utilize smart configurations in a Java application.

## Smart configurations
"
A smart configuration is capable of automatically reloading itself from time to time - no need to restart the application. Each configuration item is a key-value pair written in a special - yet simple - format, which allows the library to create the appropriate Java object according to its type.

## Just enough configuration

ReConf Client relies on a simple and straightforward .properties configuration file, just enough to know a little bit about the execution environment.

## Get rid of boilerplate code

Just create a plain old Java interface, decorate it with a few custom annotations, call a factory method and that's it! Your application is good to go.

# ReConf JVM Integration Guide

## Table of Contents

* [What are the benefits of using it?](#what-are-the-benefits-of-using-it)
* [And the minimum requirements?](#and-the-minimum-requirements)
* [How can I use it?](#how-can-i-use-it)
 * [A few concepts first](#a-few-concepts-first)
 * [Import our Maven dependency](#import-our-maven-dependency)
 * [Configure your reconf.properties file](#configure-your-reconfproperties-file)
 * [Create a Configuration Repository](#create-a-configuration-repository)
 * [Using a ConfigurationRepository](#using-a-configurationrepository)
 * [Native types built automatically](#native-types-built-automatically)
* [Advanced Features](#advanced-features)
 * [Setting up items with different characteristics](#setting-up-items-with-different-characteristics)
 * [Updating a ConfigurationRepository through code](#updating-a-configurationrepository-through-code)
 * [ConfigurationRepository reuse through Customizations](#configurationrepository-reuse-through-customizations)
 * [Organizing log messages](#organizing-log-messages)
 * [Localization with reconf.properties](#localization-with-reconfproperties)
 * [Reading from a different configuration file](#reading-from-different-configuration-file)
 * [Integrating with Spring](#integrating-with-spring)
 * [Using Customizations with Spring](#using-customizations-with-spring)
 * [Events and Listeners](#using-notifications)
* [Troubleshooting](#troubleshooting)
* [Remote Administration With ReConf Servlet](#servlet)
* [License](#license)

<a name="what-are-the-benefits-of-using-it"/>
## What are the benefits of using it?

It can provide the following capabilities to a Java application
* **Automatic update of configuration items** - from time to time the framework retrieves the latest configuration from the configuration server and handles the updated version to the application.
* **Atomic configuration retrieval** - if something fails, nothing changes. This way the application remains consistent.
* **Local cache of configuration items** - so it doesn't affect your application's resiliency and availability.
* **Native creation of Java objects** - no need to manually parse Strings and convert it to objects. The library is able to natively create Lists, Sets, Maps, primitives, Strings, arrays and every class that provides a constructor that takes a String as an argument.

<a name="and-the-minimum-requirements"/>
## And the minimum requirements?

* Java Runtime Environment 6.
* An instance of ReConf Server (or an Apache Server configured to act as one).
* A few megabytes of local storage for caching purposes (the size of it is directly proportional to the size of your configurations - the bigger they are, the bigger the space it needs).

<a name="how-can-i-use-it"/>
## How can I use it?

<a name="a-few-concepts-first"/>
### A few concepts first

Two elements form the basis of ReConf: configuration items (`@ConfigurationItem`) and configuration repositories (`@ConfigurationRepository`); simply put, one or more items grouped together form a repository. Every configuration item has a **name**, a **component** and a **product**. The latter two attributes can be defined once, in `@ConfigurationRepository`. Keep with us for more details on how to integrate!

<a name="import-our-maven-dependency"/>
### Import our Maven dependency

Add these lines to the `pom.xml` file
```xml
<dependency>
    <groupId>org.blocks4j.reconf</groupId>
    <artifactId>reconf-client</artifactId>
    <version>3.0.5</version>
</dependency>
```

<a name="configure-your-reconfproperties-file"/>
### Configure your reconf.properties file

ReConf looks for a file named **reconf.properties** in the classpath. The bare minimum configuration must have two elements, the basic URL where the ReConf server can be found (like http://reconf.myserver.com) and a directory to store the local cache (for example /export/application/local-cache).

```ini
local.cache.location=/export/application/local-cache
server.url=http://reconf.myserver.com
```

The application must have the necessary permissions to read, write and delete the directory configured in `${local.cache.location}`.

<a name="create-a-configuration-repository"/>
### Create a Configuration Repository

In order to define a configuration repository, you must create a new Java interface and decorate it with the annotations provided by the reconf-client jar.

The example below provides a very simple configuration repository where each `@ConfigurationItem` will update itself every 10 minutes.

```java
import java.math.*;
import java.util.concurrent.*;
import org.blocks4.reconf.client.annotations.*;

@ConfigurationRepository(product="my-product", component="hello-application",
    pollingRate=10, pollingTimeUnit=TimeUnit.MINUTES)
public interface Configuration {

    @ConfigurationItem("welcome.text")
    String getText();

    @ConfigurationItem("promotional.price")
    BigDecimal getPrice();
}
```

<a name="using-a-configurationrepository"/>
### Using a ConfigurationRepository

Configuration repositories are easily obtained via `get` method provided by the reconf.client.proxy.ConfigurationRepositoryFactory class. Because creating a proxy is an expensive operation, this operation caches its result; meaning that calling it one or more times will return the same instance when the same arguments are provided.

```java
    public static void main(String[] args) {
        Configuration conf = ConfigurationRepositoryFactory.get(Configuration.class);
        System.out.println(conf.getText());
    }
```

<a name="native-types-built-automatically"/>
### Native types built automatically

The library is capable of recognizing and building automatically several types of Java objects, including the interfaces from the java.util package. You can find the details below. To take full advantage of this feature, it is necessary to use the ReConf format to declare the value of your configurations.

There are two kinds of configurations: the ones that are mapped to a single object (delimited by single quotes `' '`) and sets of single objects (delimited by square brackets `[ ]`).

#### Declaring simple objects
Simple objects can be built by the tool as long as they are either primitives (or wrappers) or provide a public constructor that takes one java.lang.String as argument. The table below shows a few examples.

| returning type | configuration value | resulting object |
|----------------|-----------------------|------------------|
| ```j.l.String``` | '\n' | "\n" (a j.l.String containing the new line symbol) |
| ```boolean``` | 'tRuE' | true |
| ```j.l.Boolean``` | 'false' | false |
| ```char``` | 'a' | 'a' |
| ```j.l.Character``` | '\t' | '\t' (a j.l.Character containing the tab symbol) |
| ```j.l.Character``` | 'ab' | error! |
| ```float``` | '1.2' | 1.2 |
| ```j.m.BigDecimal``` | '1' | 1 |
| ```int``` | '10' | 10 |

> j.l = java.lang; j.m = java.math

#### Declaring arrays of objects

An array fits the "a collection of single objects" definition and so it must be declared inside square brackets. The table below shows a few examples.

| returning type | configuration value | resulting object |
|----------------|-----------------------|------------------|
| ```j.l.String``` | 'my simple String' | "my simple String" |
| ```j.l.String[ ]``` | [ 'a', ' b', 'c' ] | [ "a", " b", "c" ] |
| ```j.l.String[ ]``` | [ ] | zero-sized String array |
| ```j.l.String[ ]``` | " " | error! (no single quotes) |
| ```boolean[ ]``` | [ 'true','false' ] | [ true, false ] |
| ```int[ ]``` | [ '1' , '2' ] | [ 1, 2 ] |
| ```j.m.BigDecimal[ ]``` | [ '1', '10' ] | [ 1, 10 ] |
| ```char[ ]``` | [ 'a', '\n' ] | [ 'a', '\n' ] |
| ```float[ ]``` | [ '-1.01' ] | [ -1.01 ] |

> j.l = java.lang; j.m = java.math

#### Before we dive into Collections of objects

To group objects inside a Collection, there is no need to declare the returning type as a concrete implementation (like java.util.ArrayList or java.util.HashSet). The library is shipped with pre-selected implementations according to the type and, if you don't want to use the implementation we chose, simply use the class of choice as the returning type.

| returning type | default implementation |
|----------------|------------------------|
| ```j.u.Collection``` | ```j.u.ArrayList``` |
| ```j.u.List``` | ```j.u.ArrayList``` |
| ```j.u.Set``` | ```j.u.HashSet``` |
| ```j.u.SortedSet``` | ```j.u.TreeSet``` |
| ```j.u.NavigableSet``` | ```j.u.TreeSet``` |
| ```j.u.Queue``` | ```j.u.LinkedList``` |
| ```j.u.concurrent.BlockingQueue``` | ```j.u.concurrent.ArrayBlockingQueue``` |
| ```j.u.concurrent.BlockingDeque``` | ```j.u.concurrent.LinkedBlockingDeque``` |
| ```j.u.Map``` | ```j.u.HashMap``` |
| ```j.u.concurrent.ConcurrentMap``` | ```j.u.concurrent.ConcurrentHashMap``` |
| ```j.u.concurrent.ConcurrentNavigableMap``` | ```j.u.concurrent.ConcurrentSkipListMap``` |
| ```j.u.NavigableMap``` | ```j.u.TreeMap``` |
| ```j.u.SortedMap``` | ```j.u.TreeMap``` |

> j.u = java.util

#### Building Collections of objects

Collections of objects must be delimited by square brackets. The table below shows a few examples.

| returning type | configuration value | resulting collection |
|----------------|-----------------------|------------------|
| ```j.u.Collection<j.l.String>``` | [ 'a', 'b', 'c' ] | "a", "b", "c" |
| ```j.u.Collection<j.l.String>``` | [ ] | empty |
| ```j.u.Collection<j.u.Collection<j.l.String>>``` | [ [ ] ] | an empty collection containing an empty collection |
| ```j.u.Collection<j.u.Collection< j.l.String>>``` | [ [ 'a' ], [ 'b' ] ] | a collection containing two collections, one with "a" and the other with "b" |
| ```j.u.Collection<j.l.String>``` | ' ' | error! (no square brackets) |
| ```j.u.Collection<j.l.Boolean>``` | [ 'true', 'false' ] | true, false |
| ```j.u.Collection<j.m.BigDecimal>``` | [ '1', '10' ] | 1, 10 |
| ```j.u.Collection<j.l.Character>``` | [ 'a', 'b' ] | 'a', 'b' |
| ```j.u.Collection<j.l.Float>``` | [ '-1', '1.01' ] | -1, 1.01 |

> j.l = java.lang; j.m = java.math; j.u = java.util

#### Building Maps
A Map is different from a Collection because it contains pairs of tuples of the form Key-Value, whereas a Collection is a container of objects with no relation among themselves. For this reason, the formatting part is different, but not that much, since a Map is a complex type. Just separate a key from its value by using a colon `:` and a pair of key-values from each other using a comma `,`.

| returning type | configuration value | resulting map |
|----------------|-----------------------|------------------|
| ```j.u.Map<j.l.String,j.l.Character>``` | [ 'a':'b' , 'c':'d' ] | { "a" = 'b', "c" = 'd' } |
| ```j.u.Map<j.l.Object,j.l.Object>``` | [ ] | empty |
| ```j.u.Map<j.l.String,j.u.List<j.l.Object>>``` | [ 'k' : [ ] ] | { "k" = [ ] } (the key "k" maps to an empty collection) |
| ```j.u.Map<j.l.String,j.u.List<j.l.Integer>>``` | [ 'k' : [ '1','2' ] ] | { "k" = [ 1,2 ] } |
| ```j.u.Map<j.l.Object,j.l.Object>``` | ' ' | error! (no square brackets) |
| ```j.u.Map<j.l.String,j.l.String>``` | [ 'x' ] | error! (a key must map to a value) |
| ```j.u.Map<j.l.String,j.l.String>``` | [ 'x' : 1 ] | error! (no single quotes enclosing 1) |
| ```j.u.Map<j.l.String,j.l.Boolean>``` | [ 'true' : 'false' ] | { "true" = false } |
| ```j.u.Map<j.l.Integer,j.l.Integer>``` | [ '1' : '2' ] | { 1 = 2 } |

> j.l = java.lang; j.u = java.util

<a name="advanced-features"/>
## Advanced Features

<a name="setting-up-items-with-different-characteristics"/>
### Setting up items with different characteristics

It is possible to specialize an item, setting it up with characteristics that deviate from the ones configured in the `@ConfigurationRepository` annotation. The library operates using a simple rule: when inspecting an item, if nothing is found, the item will inherit the attributes defined in the interface. Otherwise, just the particular difference found will be applied.

#### Reading the configuration from another component and/or product

In the example below, "currency.code" belongs to another component, named "goodbye-component". The fourth item, "minimum.age" is part of the "general-configuration" component, which belongs to "all-products" product. The other two items, "promotional.price", and "welcome.text" belong to "hello-application" which is under "my-product". Finally, all items will update on a ten-second basis.

```java
import java.math.*;
import java.util.concurrent.*;
import org.blocks4.reconf.client.annotations.*;

@ConfigurationRepository(product="my-product", component="hello-application",
    pollingRate=10, pollingTimeUnit=TimeUnit.MINUTES)
public interface Configuration {

    @ConfigurationItem("welcome.text")
    String getText();

    @ConfigurationItem("promotional.price")
    BigDecimal getPrice();

    @ConfigurationItem(value="currency.code", component="goodbye-application")
    String getCurrencyCode();

    @ConfigurationItem(value="minimum.age", component="general-configuration",
        product="all-products")
    int getMinimumAge();
}
```

<a name="updating-a-configurationrepository-through-code"/>
### Updating a ConfigurationRepository through code

There's a way to force an update operation of every `@ConfigurationItem` of a `@ConfigurationRepository`, regardless the update frequency parameters (rate and timeUnit). To enable it, add a **void** method to the interface and annotate it with `@UpdateConfigurationRepository`. When called, the method will block until all update operations have returned. In case everything goes ok, the local cache is updated; otherwise a runtime `UpdateConfigurationRepositoryException` is thrown to notify the application that a problem has occurred.

```java
package examples;

import java.util.*;
import java.util.concurrent.*;
import org.blocks4.reconf.client.annotations.*;

@ConfigurationRepository(product="my-product", component="hello-application",
    pollingRate=1, pollingTimeUnit=TimeUnit.HOURS)
public interface Configuration {

    @ConfigurationItem("welcome.text")
    String getText();

    @UpdateConfigurationRepository
    void updateIt();
}
```

<a name="configurationrepository-reuse-through-customizations"/>
### ConfigurationRepository reuse through Customizations

Customizations are a feature that allows the developer to solve the following problem: "Can I create two instances of the same ConfigurationRepository containing different configuration values?". In order to do that, we introduce the concept of Customizations. This feature provides a way to slightly change a configuration repository by adding prefixes and/or suffixes for components and/or configuration item names.

```java
    public static void main(String[] args) {
        Customization cust = new Customization();
        cust.setComponentPrefix("cp-");
        cust.setComponentSuffix("-cs");
        cust.setComponentItemPrefix("kp-");
        cust.setComponentItemSuffix("-ks");

        Configuration conf = ConfigurationRepositoryFactory.get(Configuration.class);

        Configuration customConf = ConfigurationRepositoryFactory.get(Configuration.class, cust);
        System.out.println(conf.getText() + ", " + customConf.getText());
    }
```

The example above creates two repositories, both from the same interface. The "conf" repository will behave just as expected, retrieving the configuration from "my-product/hello-application/welcome.text" hierarchy. The second instance though, named "customConf", will retrieve the configuration from "my-product/cp-hello-application-cs/kp-welcome.text-ks" hierarchy.

<a name="organizing-log-messages"/>
### Organizing log messages

If you use slf4j (if you don't there are [a lot of reasons](http://logback.qos.ch/reasonsToSwitch.html) to do it), you can declare a new logger and append it to the appender of your preference. The example sets the level to DEBUG but I don't recommend doing it in production environment since it's very verbose. The INFO level will do just fine.

```xml
<logger name="ReConf" additivity="false" level="DEBUG">
    <appender-ref ref="A1" />
</logger>
```

<a name="localization-with-reconfproperties"/>
### Localization with reconf.properties

To activate localized log messages, add a property named `locale` in the reconf.properties file. The locale must comply with the [JDK 6 and JRE 6 Supported Locales](http://www.oracle.com/technetwork/java/javase/locales-137662.html). Besides the default locale (en_US) the library also provides an additional one, Portuguese Brazil (pt_BR).

```ini
locale=pt_BR
local.cache.location=/export/application/local-cache
server.url=http://reconf.myserver.com
```

<a name="reading-from-different-configuration-file"/>
### Reading from a different configuration file
To read a different configuration file, start the application with `-Dreconf.client.file.location=/path/to/a/different/file.properties`. The library will try to read from this location before looking for a reconf.properties file in the classpath.

<a name="integrating-with-spring"/>
### Integrating with Spring

The package `reconf-spring` provides a class for easy integration with Spring, including the use of `@Autowired` annotation. Add the following dependency to the `pom.xml` file.

```xml
<dependency>
    <groupId>br.com.uol.reconf</groupId>
    <artifactId>reconf-spring</artifactId>
    <version>${project.version}</version>
</dependency>
```

For every configuration repository, declare a bean of class "org.blocks4j.reconf.spring.RepositoryConfigurationBean" with a "configInterface" attribute configured with the interface containing the `@ConfigurationRepository` annotation.

Assuming that we are using the interface below.
```java
package example;

import java.util.concurrent.*;
import org.blocks4.reconf.client.annotations.*;

@ConfigurationRepository(product="my-product", component="hello-application",
    pollingRate=10, pollingTimeUnit=TimeUnit.MINUTES)
public interface Configuration {

    @ConfigurationItem("welcome.text")
    String getText();
}
```

The xml should look like this.

```xml
<bean class="org.blocks4j.reconf.spring.RepositoryConfigurationBean">
    <property name="configInterface" value="example.Configuration" />
</bean>
```

<a name="using-customizations-with-spring"/>
### Using Customizations with Spring

The xml excerpt below creates two beans, a regular "conf" and a custom "customConf" detailed in [ConfigurationRepository reuse through Customizations](#configurationrepository-reuse-through-customizations).

```xml
<bean id="customConf" class="org.blocks4j.reconf.spring.RepositoryConfigurationBean">
    <property name="configInterface" value="example.Configuration"/>
    <property name="componentPrefix" value="cp-"/>
    <property name="componentSuffix" value="-cs"/>
    <property name="componentItemPrefix" value="kp-"/>
    <property name="componentItemSuffix" value="-ks"/>
</bean>

<bean id="conf" class="org.blocks4j.reconf.spring.RepositoryConfigurationBean">
    <property name="configInterface" value="example.Configuration"/>
</bean>
```

<a name="using-notifications"/>
### Events and Listeners

The API fires notifications for certain kinds of events. The current version is capable of notifying clients of two significative events related to the `@ConfigurationItem` lifecycle: (1) the property is successfully updated; (2) the client is not able to build an object from the obtained value.

To listen to such events, the application developer must provide an implementation of the `reconf.client.notification.ConfigurationItemListener` interface. There are two possible ways to register the listener. One is by adding it directly into the [Customization](#configurationrepository-reuse-through-customizations) object (example 1). The other is during the beanwiring set up on Spring (example 2).

```java
    // example 1
    public static void main(String[] args) throws Exception {
        Customization custom = new Customization();
        custom.addConfigurationItemListener(new ConfigurationItemListener() {

            public void onEvent(UpdateNotification event) {
                System.out.println("updated value [" + event.getRawValue() +
                    "] read from [" + event.getSource() + "]");
            }

            public void onEvent(ErrorNotification event) {
                System.out.println("error while updating a property. obtained value [" +
                    event.getRawValue() + "] read from [" + event.getSource() +
                    "] exception [" + event.getError() + "]");
            }
        });
        Configuration conf = ConfigurationRepositoryFactory.get(Configuration.class, custom);
        System.out.println(conf.getText());
    }
```

```xml
    <!-- example 2 -->
    <bean id="conf" class="org.blocks4j.reconf.spring.RepositoryConfigurationBean">
        <property name="configInterface" value="org.blocks4j.reconf.driver.Configuration"/>
        <property name="configurationItemListeners">
            <util:list>
                <bean class="org.blocks4j.reconf.driver.MyListener"/>
            </util:list>
        </property>
    </bean>
```

The basic attributes shared by both Notification events are:
* `java.lang.String` product
* `java.lang.String` component
* `java.lang.String` item
* `java.lang.reflect.Method` method
* `java.lang.Class` cast
* `java.lang.String` rawValue
* `reconf.client.config.update.ConfigurationItemUpdateResult.Source` source

The `ErrorNotification` class has an additional `java.lang.Throwable` throwable field, whilst the `UpdateNotification` class has a `java.lang.Object` newValue field containing the newly created object.

<a name="troubleshooting"/>
## Troubleshooting

1. Check for error and warn messages logged by the framework.
2. Check for "strange" characters in your configuration item values, such as tabs and spaces, before and after enclosing characters (`''` and `[]`).
3. Try to delete everything inside the local-cache directory defined in the reconf.xml file.
4. Enable DEBUG logging and look for strange messages.

<a name="servlet"/>
# Remote Administration With ReConf Servlet

The package `reconf-servlet` enables users and/or applications the remote administration of certain aspects of the framework via an HTTP API. Add the following dependency to the `pom.xml` file.

```xml
<dependency>
    <groupId>br.com.uol.reconf</groupId>
    <artifactId>reconf-servlet</artifactId>
    <version>${project.version}</version>
</dependency>
```

For now, the only functionality available is "sync", which results in immediate reload of every `@ConfigurationRepository` active. Internally, the framework queries the current value of all `@ConfigurationItem` in the reconf-server. If one of them fails, the sync is cancelled and a rollback is made. The rollback only applies to the specific `@ConfigurationRepository` that failed. Overall result is returned as a JSON, with distinct "success" fields for every repository.

To use it, map the `reconf.servlet.AdminServlet` and fire an HTTP GET request.

```sh
# displays the option menu
curl -G -v localhost:8080/reconf

# calls the sync functionality
curl -G -v localhost:8080/reconf/sync
```

## Integration with Web Applications

If you are running ReConf inside a web application, just declare the servlet inside `WEB-INF/web.xml` file and choose a mapping pattern to access it, like the example below.

```xml
<servlet>
    <servlet-name>reconf</servlet-name>
    <servlet-class>reconf.servlet.AdminServlet</servlet-class>
</servlet>
<servlet-mapping>
    <servlet-name>reconf</servlet-name>
    <url-pattern>/reconf/*</url-pattern>
</servlet-mapping>
```

## Integration with Standalone Applications

In a standalone application, there is the need to start an embedded servlet container in order to make the `reconf.servlet.AdminServlet` available.

The snippet below assumes that the family of `org.eclipse.jetty` packages (jetty-server and jetty-servlet) are configured under the dependencies tag of the `pom.xml` file.

```java
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.servlet.*;
import org.blocks4.reconf.servlet.*;

public class ReconfServlet {

    public static void main(String[] args) throws Exception {
        Server server = new Server(8080);
        ServletContextHandler handler = new ServletContextHandler();
        handler.setContextPath("/");
        handler.addServlet(AdminServlet.class, "/reconf/*");
        server.setHandler(handler);
        server.start();
    }
}
```

<a name="license"/>
# License

Copyright 2013-2015 Blocks4J Team (www.blocks4j.org)

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
