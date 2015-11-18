# forsuredb
For an in-depth explanation of the project, see [the project wiki](https://github.com/ryansgot/forsuredb/wiki). The following is just an Android quick-setup guide.

## Using forsuredb in Android
- Create a new Android project
- Set up the project build.gradle repositories and dependencies like this:
```groovy
buildscript {
    repositories {
        jcenter()
    }
    dependencies {
        classpath 'com.android.tools.build:gradle:1.2.3'
        classpath 'com.neenbedankt.gradle.plugins:android-apt:1.6'
        classpath 'com.fsryan:forsuredbplugin:0.0.3'
    }
}

allprojects {
    repositories {
        jcenter()
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
    
    compile 'com.fsryan:forsuredbandroid:0.2.1@aar'
    compile 'com.fsryan:forsuredbcompiler:0.2.1'
    
    apt 'com.fsryan:forsuredbcompiler:0.2.1'
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
- Declare an application class and the ```FSDefaultProvider``` in your app's AndroidManifest.xml file:
```xml
<application
        android:name=".App" >
  <!-- ... -->
  <provider
      android:name="com.forsuredb.provider.FSDefaultProvider"
      android:authorities="com.forsuredb.testapp.content"
      android:enabled="true"
      android:exported="false" />
</application
```
- Create the App class that you declared above
```java
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        FSDBHelper.init(this, "testapp.db", TableGenerator.generate());         // <-- creates the tables based upon your FSGetApi extensions
        ForSureAndroidInfoFactory.init(this, "com.forsuredb.testapp.content");  // <-- the String is your Content Provider's authority
        ForSure.init(ForSureAndroidInfoFactory.inst());  // <-- ForSureAndroidInfoFactory tells ForSure everything it needs to know.
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
## Supported Migrations
- Add a table
- Add a column to a table
- Add a unique index column to a table
- Make an existing column a unique index
- Add a foreign key column to a table

## Coming up
- stricter testing
- support for more types of migrations
- an example java (non-Android) project and corresponding forsuredbjava library
- more robust where-clause editing when doing joins
- JavaPoet integration for cleaner source file generation code
- A solution for the issue adding multiple ```@ForeignKey``` annotations to the same ```FSGetApi``` extension at once
- support for multiple autojoins between the same tables
- plugin-style database support extensions
