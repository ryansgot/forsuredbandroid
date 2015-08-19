# forsuredb
forsuredb is a project designed to take a lot of the work out of database programming. It was inspired mainly by the retrofit project (https://github.com/square/retrofit), and it is meant to be a typesafe, quick means of defining and working with data. forsuredb is not intended to replace existing frameworks (see the Android Development subheading below)

## Possible Use Cases
1. Android Development
2. Some other java project

### Android Development
The original intention of the project was to make database programming in Android take less boilerplate code. If you always follow the examples available to you via http://developer.android.com/ or many other sites, then you will find yourself writing highly verbose code that works its way into being unreadable quickly. You'll also end up writing one-off code a lot of places when you do migrations.

### Some other Java Project
I can't give clear instructions on how to do this right now. Every interface you'll need to adapt to your setup is defined in the compiler project, so you can just implement those interfaces, following the example in the forsuredblib project. The ```QueryGenerator``` extensions for SQLite are provided, so it would be easiest if you used SQLite, but that's not a requirement.

## Using forsuredb in Android
- Create a new Android project
- Create a ContentProvider (with ```"my.provider.authority"``` as the authority--or whatever you want)
- Define an interface
```java
@FSTable("user")
public interface UserTable extends FSGetApi {
    @FSColumn("_id") @PrimaryKey long id(Cursor cursor);
    @FSColumn("global_id") long globalId(Cursor cursor);
    @FSColumn("login_count") int loginCount(Cursor cursor);
    @FSColumn("app_rating") double appRating(Cursor cursor);
    @FSColumn("competitor_app_rating") BigDecimal competitorAppRating(Cursor cursor);
}
```
- Initialize ForSure in your Application
```java
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // initialize ForSure
        FSTableCreator userTable = new FSTableCreator("my.provider.authority", UserTable.class);
        ForSure.init(this, "test.db", Lists.newArrayList(userTable));
    }
}
```
- Migrate the database (until I create a gradle plugin, take a look at the dbmigrate task in app/build.gradle file of this repo)
```
./gradlew dbmigrate
```
After you do this, then you can get stuff from the database by:
```java
Uri usersUri = ForSure.inst().getTable("user").getAllRecordsUri();
Cursor cursor = getContentResolver().query(usersUri, null, null, null, null);
UserTable userTable = ForSure.inst().getApi(userUri);
userTable.id(cursor);
userTable.globalId(cursor);
/* etc */
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
```
_Notice that you DID NOT have to write the UserTableSetter class above. It gets generated for you at compile time._ Moreover, you did not have to write the implementation of either your interface or the corresponding Setter interface that got generated.

In fact, _migrations also get generated for you_ as XML assets when you run ```./gradlew dbmigrate``` (the parser is provided for you when you under-the-hood when you use ```MigrationRetrieverFactory```). These XML assets will then be placed in your classpath next time you build. Since the forsuredblib project contains the SQLiteOpenHelper class, it figures out the version number of your database for you (based upon the migrations) and applies the necessary migrations on upgrade or all migrations on create of the database.

So if you want to migrate your database after its initial creation . . . say to add a table, then you would just:
- Define another interface
```java
@FSTable("profile_info")
public interface ProfileInfoTable extends FSGetApi {
    @FSColumn("_id") @PrimaryKey long id(Cursor cursor);
    @FSColumn("user_id") @ForeignKey(apiClass = UserTable.class, columnName = "_id") long userId(Cursor cursor);
    @FSColumn("email_address") String emailAddress(Cursor cursor);
    @FSColumn("binary_data") byte[] binaryData(Cursor cursor);
}
```
- Create a new FSTableCreator object to initialize ForSure with:
```java
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // initialize ForSure
        FSTableCreator userTable = new FSTableCreator("my.provider.authority", UserTable.class);
        FSTableCreator profileInfoTable = new FSTableCreator("my.provider.authority", ProfileInfoTable.class);
        ForSure.init(this, "test.db", Lists.newArrayList(userTable, profileInfoTable));
    }
}

```
- Migrate the database
```
./gradlew dbmigrate
```

That's it. No messy one-off code--no necessary SQL. Just as before, a corresponding Setter interface gets generated for you at compile time, so you can use it in your app.

## Coming up
- A gradle plugin containing the dbmigrate task
- An SQLite QueryGenerator for specifying that a column is unique
- A refactor of the FSGetAdapter and FSSaveAdapter classes.
