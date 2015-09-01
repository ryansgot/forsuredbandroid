# forsuredb
forsuredb is a project designed to take a lot of the work out of database programming. It was inspired mainly by the retrofit project (https://github.com/square/retrofit), and it is meant to be a typesafe, quick means of defining and working with data. forsuredb is not intended to replace existing frameworks (see the Android Development subheading below)

## Possible Use Cases
1. Android Development
2. Some other java project

### Android Development
The original intention of the project was to make database programming in Android take less boilerplate code. If you always follow the examples available to you via http://developer.android.com/ or many other sites, then you will find yourself writing highly verbose code that works its way into being unreadable quickly. You'll also end up writing one-off code a lot of places when you do migrations.

### Some other Java Project
Having only done this for the example Android project, I'm not quite sure about all of the details to make this work in your own project, but a good place to start would be ...
- Implement the following interfaces
  - FSQueryable&lt;U, R&gt;, where U is the type parameter of the SaveResult interface that you want (should match the ```resultParameter``` System property set in the build.gradle file), and R is the class that prepares a record for insert or update queries. Take a look at the ```ContentProviderQueryable``` class for an example.
  - RecordContainer, the container for a record that prepares the record for insertion and update queries. Take a look at ```FSContentValues``` for an example.
  - Retriever, the class that can retrieve records from your database. Take a look at ```FSCursor``` for an example.
- Write Some code to retrieve migrations from the generated migration XML assets. You _DO NOT_ have to write your own XML parsing code. This is already done for you. You just have to use the ```MigrationRetrieverFactory``` class to create an appropriate migration retriever.
- Use SQLite as your database if you intend to make use of the migration capability of forsuredb. All of the provided migration SQL presupposes SQLite at the moment.

## Using forsuredb in Android
- Create a new Android project
- Set up the project build.gradle buildscript repositories and dependencies like this:
```groovy
buildscript {
    repositories {
        jcenter()
        // the following is necessary until the plugin is hosted on jcenter
        maven {
            url  "http://dl.bintray.com/ryansgot/maven"
        }
    }
    dependencies {
        classpath 'com.android.tools.build:gradle:1.2.3'
        classpath 'com.neenbedankt.gradle.plugins:android-apt:1.6'
        classpath 'com.fsryan:forsuredbplugin:0.0.3'
    }
}
```
- Amend your app build.gradle file as such:
```groovy
apply plugin: 'com.android.application'
apply plugin: 'android-apt'
apply plugin: 'com.fsryan.forsuredb'

/* etc*/

forsuredb {
    applicationPackageName = 'com.forsuredb.testapp'
    resultParameter = "android.net.Uri"               // the fully-qualified class name of the parameterization of the SaveResult.
                                                      // If you have an Android project, this should be the result parameter.
    dbType = 'sqlite'                                 // this does nothing at the moment. Only sqlite is supported.
    migrationDirectory = 'app/src/main/assets'        // the assets directory of your app starting at your project's base directory
    appProjectDirectory = 'app'
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
/*
 * The Retriever implementation for Android projects is defined for you as the FSCursor class--which itself is a Cursor
 */
```
- Initialize ForSure in your Application (note that your Application class must be defined in your application's root package at the moment). The TableGenerator class is generated at compile time, so your IDE may give you a compilation error--ignore it. Maybe someday I'll make an Android Studio plugin.
```java
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // initialize ForSure. You can choose to pass in a database name or not.
        ForSure.init(this, TableGenerator.generate());
    }
}
/*
 * NOTICE: The TableGenerator class is generated for you at compile time, so until you compile the project once, your IDE won't
 * find this class
 */
```
- Migrate the database (until I create a gradle plugin, take a look at the dbmigrate task in app/build.gradle file of this repo)
```
./gradlew dbmigrate
```
After you do this, then you can retrieve records from the database by:
```java
Uri usersUri = ForSure.inst().getTable("user").getAllRecordsUri();
Retriever retriever = new FSCursor(getContentResolver().query(usersUri, null, null, null, null));
UserTable userTable = ForSure.inst().getApi(userUri);
userTable.id(retriever);
userTable.globalId(retriever);
/* etc */
/*
 * NOTICE: you didn't have to define the id method in the UserTable interface because it extends the FSGetApi interface,
 * where the id, created, deleted, and modified methods are defined for you.
 */
```
You can put stuff into the database by:
```java
ForSure.inst().setApi(UserTableSetter.class)  // You can also retrieve the setApi with a Uri
        .appRating(4.1D)
        .competitorAppRating(new BigDecimal(4.2))
        .globalId(8439L)
        .id(8493L)
        .loginCount(5)
        .save();
/*
 * When you run the save method, an upsert is performed. That is, if the record with the _id specified does not exist, a new one
 * is inserted. When the record exist, an update is performed. If an update is performed, then the modified column trigger updates
 * the modified date to the date the record was modified.
 */
```
_Notice that you DID NOT have to write the UserTableSetter class above. It gets generated for you at compile time._ Moreover, you did not have to write the implementation of either your interface or the corresponding Setter interface that got generated.

In fact, _migrations also get generated for you_ as XML assets when you run ```./gradlew dbmigrate```. These XML assets will then be placed in your classpath next time you build. Since the forsuredblib project contains the SQLiteOpenHelper class, it figures out the version number of your database for you and applies the necessary migrations on upgrade or all migrations on create of the database.

So if you want to migrate your database after its initial creation (in this case, to add a table), then you would just:
- Define another interface
```java
@FSTable("profile_info")
public interface ProfileInfoTable extends FSGetApi {
    @FSColumn("_id") @PrimaryKey long id(Retriever retriever);
    @FSColumn("user_id") @ForeignKey(apiClass = UserTable.class, columnName = "_id") long userId(Retriever retriever);
    @FSColumn("email_address") String emailAddress(Retriever retriever);
    @FSColumn("binary_data") byte[] binaryData(Retriever retriever);
}
```
NOTICE: As of this README, you cannot add more than one ```@ForeignKey``` column to the same table when you run ```./gradlew dbmigrate```. A workaround if you want to add multiple ```@ForeignKey``` columns is to run ```./gradlew dbmigrate``` in between addition of each ```@ForeignKey``` column.
- Migrate the database
```
./gradlew dbmigrate
```

That's it. No messy one-off code--no need to write your own SQL or migrations--just write java interfaces. Just as before, a corresponding Setter interface gets generated for you at compile time, so you can use it in your app.

## Supported Migrations
- Add a table
- Add a column to a table
- Add a unique index column to a table
- Add a foreign key column to a table

## Coming up
- A gradle plugin containing the dbmigrate task
