# forsuredb
forsuredb is a project designed to take a lot of the work out of database programming. Inspired mainly by the retrofit project (https://github.com/square/retrofit) and ActiveRecord (https://github.com/rails/rails/tree/master/activerecord), forsuredb is intended to be a typesafe, quick means of defining and working with data. It is not intended to replace existing frameworks, but to work within them (see the Android Development subheading below).

For a video guide (15 minutes total) to setting up a forsuredb project in Android, see [the forsuredb introduction](https://www.youtube.com/watch?v=Z7uyuIkmYCk&list=PLVvK5rk-nDhJByVqhwdsf4icd2XBUDrtV).

## Possible Use Cases
1. Android Development
  * The integration work has already been done for you in the forsuredbandroid module
2. Some other java project
  * There is some integration work remaining, but you should be able to get a handle for how to do it by taking a look at the forsuredbandroid module. See the 'Some other Java Project' subheading below for instructions.

### Android Development
The original intention of the project was to reduce the amount of sprinkled boilerplate code when doing database programming in Android. If you always follow the examples available to you via http://developer.android.com/ or many other instructional sites, then you will find yourself writing highly verbose code that works its way into being unreadable quickly. You'll also end up writing one-off code a lot of places when you do migrations. The code in the compiler java library and the forsuredbandroid android library make this sort of programming quite a bit easier.

### Some other Java Project
Having only done this for the example Android project, I'm not quite sure about all of the details to make this work in your own project, but a good place to start would be ...
- Implement the following interfaces
  - FSQueryable&lt;U, R&gt;, where U is the type parameter of the SaveResult interface that you want (should match the ```resultParameter``` System property set in the build.gradle file), and R is the class that prepares a record for insert or update queries. Take a look at the ```ContentProviderQueryable``` class for an example.
  - RecordContainer, the container for a record that prepares the record for insertion and update queries. Take a look at ```FSContentValues``` for an example.
  - Retriever, the class that can retrieve records from your database. Take a look at ```FSCursor``` for an example.
- Write Some code to retrieve migrations from the generated migration XML assets. You _DO NOT_ have to write your own XML parsing code. This is already done for you. You just have to use the ```MigrationRetrieverFactory``` class to create an appropriate migration retriever.
- Use SQLite as your database if you intend to make use of the migration capability of forsuredb. All of the provided migration SQL presupposes SQLite at the moment.

## Using forsuredb in Android
### Setup
- Create a new Android project
- Set up the project build.gradle repositories and dependencies like this:
```groovy
buildscript {
    repositories {
        jcenter() // <-- hosts the forsuredbplugin binary
    }
    dependencies {
        classpath 'com.android.tools.build:gradle:1.2.3'
        classpath 'com.neenbedankt.gradle.plugins:android-apt:1.6'
        classpath 'com.fsryan:forsuredbplugin:0.0.3'
    }
}

allprojects {
    repositories {
        jcenter() // <-- hosts the forsuredbcompiler and forsuredbandroid binaries
    }
}
```
- Amend your app build.gradle file as such:
```groovy
apply plugin: 'com.android.application'
apply plugin: 'android-apt'             // <-- enables the forsuredbcompiler
apply plugin: 'com.fsryan.forsuredb'    // <-- provides the dbmigrate task

android {
    packagingOptions {
        exclude 'META-INF/LICENSE.txt'  // <-- the forsuredbcompiler relies upon the apache velocity project, which packages a META-INF/LICENSE.txt file. 
        exclude 'META-INF/NOTICE.txt'   // <-- the forsuredbcompiler relies upon the apache velocity project, which packages a META-INF/NOTICE.txt file. 
    }
}

dependencies {
    compile 'com.google.guava:guava:18.0' // <-- forsuredbandroid depends on this, but the version probably doesn't matter much
    
    compile 'com.fsryan:forsuredbandroid:0.0.3@aar'
    compile 'com.fsryan:forsuredbcompiler:0.0.3'
    
    apt 'com.fsryan:forsuredbcompiler:0.0.3'
}

forsuredb {
    applicationPackageName = 'com.forsuredb.testapp'
    resultParameter = "android.net.Uri"               // the fully-qualified class name of the parameterization of the SaveResult.
                                                      // If you have an Android project, this should be the result parameter.
    dbType = 'sqlite'                                 // this does nothing at the moment. Only sqlite is supported.
    migrationDirectory = 'app/src/main/assets'        // the assets directory of your app starting at your project's base directory
    appProjectDirectory = 'app'                       // Your app's base directory
}
```
- Use the default content provider by declaring it in your app's AndroidManifest.xml file:
```xml
<!--
  - com.forsuredb.testapp.content is the default authority. You can use any authority you wish, and 
  - I strongly encourage you to use a different authority because authority Strings are intended to
  - be globally unique. Thus, if you use the default authority, then you will not be able to
  - release your app to the app store.d
-->
<provider
    android:name="com.forsuredb.provider.FSDefaultProvider"
    android:authorities="com.forsuredb.testapp.content"
    android:enabled="true"
    android:exported="false" />
```
- Specify your application's ```android:name``` attribute in your app's AndroidManifest.xml file and create the corresponding extension of the ```Application``` class:
```xml
<application
        android:name=".App" >
```
```java
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // initialize ForSure. You can choose to pass in a database name or not.
        ForSure.init(this, TableGenerator.generate());
        // if your ContentProvider's authority is not app.package.name.content, then you need to pass
        // the authority string as an argument to TableGenerator.generate("my.contentprovider.authority");
    }
}
```
- Define an interface that extends FSGetApi
```java
@FSTable("user")
public interface UserTable extends FSGetApi {   // <-- you must extend FSGetApi when @FSTable annotates an interface or your app won't compile
    @FSColumn("global_id") long globalId(Retriever retriever);
    @FSColumn("login_count") int loginCount(Retriever retriever);
    @FSColumn("app_rating") double appRating(Retriever retriever);
    @FSColumn("competitor_app_rating") BigDecimal competitorAppRating(Retriever retriever);
}
```
- Migrate the database by using the dbmigrate gradle task (defined by forsuredbplugin)
```
./gradlew dbmigrate
```
After you do this, then you can retrieve records from the database by:
```java
UserTable userTable = ForSure.resolve("user").getter();
Retriever retriever = ForSure.queryAll("user")
long id = userTable.id(retriever);    // <-- you get the the id, created, deleted, and modified methods for free
Date created = userTable.created(retriever);
long globalId = userTable.globalId(retriever);
/* etc */
```
You can update/insert to the database by:
```java
UserTableSetter setter = ForSure.resolve("user").setter();
SaveResult<Uri> sr = setter.appRating(1.25D)
        .competitorAppRating(new BigDecimal(1.5F))
        .globalId(234846L)
        .id(1L)
        .loginCount(1234)
        .save();
```
_Notice that you DID NOT have to write the UserTableSetter class above. It gets generated for you at compile time._ Moreover, you did not have to write the implementation of either your interface or the corresponding Setter interface that got generated.
### Migration
In fact, _migrations also get generated for you_ as XML assets when you run ```./gradlew dbmigrate```. These XML assets will then be placed in your classpath next time you build. forsuredbandroid figures out all of the details behind migrating the database for you given the assets generated by ```./gradlew dbmigrate```--so the only code you have to write is in your extensions of the ```FSGetApi``` interface.

So if you want to migrate your database after its initial creation (in this case, to add a table), then you would just:
- Define another interface
```java
@FSTable("profile_info")
public interface ProfileInfoTable extends FSGetApi {
    @FSColumn("user_id") @ForeignKey(apiClass = UserTable.class, columnName = "_id") long userId(Retriever retriever);
    @FSColumn("email_address") String emailAddress(Retriever retriever);
    @FSColumn("binary_data") byte[] binaryData(Retriever retriever);
}
```
As of this README, you cannot add more than one ```@ForeignKey``` column to the same table when you run ```./gradlew dbmigrate```. A workaround if you want to add multiple ```@ForeignKey``` columns is to run ```./gradlew dbmigrate``` in between addition of each ```@ForeignKey``` column.
- Migrate the database
```
./gradlew dbmigrate
```

That's it. No messy one-off code--no need to write your own SQL or migrations--just write java interfaces. Just as before, a corresponding Setter interface gets generated for you at compile time, so you can use it in your app.
### Static Data
You can define static data as XML in your assets directory. for example, app/src/main/assets/profile_info.xml is below:
```xml
<resources>
    <profile_info _id="1" user_id="5" email_address="user_id_5@email.com" binary_data="42" />
    <profile_info _id="2" user_id="4" email_address="user_id_4@email.com" binary_data="42" />
    <profile_info _id="3" user_id="3" email_address="user_id_3@email.com" binary_data="42" />
    <profile_info _id="4" user_id="2" email_address="user_id_2@email.com" binary_data="42" />
    <profile_info _id="5" user_id="1" email_address="user_id_1@email.com" binary_data="42" />
    <profile_info _id="6" user_id="6" email_address="user_id_6@email.com" binary_data="42" />
    <profile_info _id="7" user_id="7" email_address="user_id_7@email.com" binary_data="42" />
    <profile_info _id="8" user_id="8" email_address="user_id_8@email.com" binary_data="42" />
    <profile_info _id="9" user_id="9" email_address="user_id_9@email.com" binary_data="42" />
</resources>
```
If you do this, then you notify the compiler that you'd like to use this file by annotating your extension of the ```FSGetApi``` interface with ```@StaticData``` as below:
```java
@FSTable("profile_info")
@FSStaticData(asset = "profile_info.xml", recordName = "profile_info")
public interface ProfileInfoTable extends FSGetApi {
    @FSColumn("user_id") @ForeignKey(apiClass = UserTable.class, columnName = "_id") long userId(Retriever retriever);
    @FSColumn("email_address") String emailAddress(Retriever retriever);
    @FSColumn("binary_data") byte[] binaryData(Retriever retriever);
}
```
For Android projects, this means that even when the user deletes all data, after all of the migrations are rerun, the static data will get reinserted.
## Supported Migrations
- Add a table
- Add a column to a table
- Add a unique index column to a table
- Add a foreign key column to a table

## Coming up
- stricter testing
- record deletion support
- default cascading behaviors
- support for more types of migrations
- an example java (non-Android) project and corresponding forsuredbjava library
- Fluent Find api for querying the database
- automatically-generated join interfaces based upon foreign key relationships
- plugin-style database support extensions
