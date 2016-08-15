# forsuredbandroid

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
