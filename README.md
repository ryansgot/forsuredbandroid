# forsuredbandroid

Forsuredbandroid is the android framework component of the forsuredb project. If you're thinking about using forsuredb on Android, you should definitely use this library. It provides some handy tools like:
- ```FSCursorLoader``` for writing custom ```LoaderManager.Callbacks``` that integrates nicely with the forsuredb ```Resolver``` concept
- ```FSAndroidSQLiteGenerator```, an implementation of the SQLite DBMS integration that handles problems with ```SQLiteCursor``` and allows you to succcessfully join tables that have columns with the same name.
- ```FSCursorRecyclerViewAdapter``` class that handles boilerplate for adapting data from an ```FSCursor```
- ```FSDBHelper``` class to integrate with ```SQLiteOpenHelper```

## Quick Start Guide
See https://github.com/ryansgot/forsuredbcompiler/blob/master/README.md#using-forsuredb-in-android

## Proguard Considerations
forsuredb leverages ```javax.annotation.processing.AbstractProcessor```, reflection and ```java.lang.reflect.Proxy```, so if you use proguard to minify your project, you'll have to include the following lines in your proguard-rules.pro file:
```
-dontwarn com.fsryan.forsuredb.annotationprocessor.FSAnnotationProcessor
-keepattributes Signature,*Annotation*
-keepparameternames
-keep class com.fsryan.forsuredb.migration.Migration$Type { *; }
-keep class com.fsryan.forsuredb.annotations.ForeignKey$ChangeAction { *; }
-keep interface com.fsryan.forsuredb.api.FSSaveApi { <methods>; }
-keep interface com.fsryan.forsuredb.api.FSGetApi { <methods>; }
-keep interface com.fsryan.forsuredb.api.Retriever { <methods>; }

# keep all of the generated dbmodel interfaces and generated classes/interfaces
# It is a good idea to keep all of your extensions of FSGetApi in one package.
# If you do this, then you can replace the packages below with your packages:
-keep class com.my.db.model.package.* { *; }
-keep interface com.my.db.model.package.* { *; } 
```
