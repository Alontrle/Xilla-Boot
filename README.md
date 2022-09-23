# XillaBoot

Xilla Boot makes it easy to handle large amounts of data efficiently in java applications. This library can streamline development of all types of projects, from hobby projects to large scale applications. The interface is designed to be as user friendly as possible.

This library is built ontop of gson and uses it to automatically serialize/deserialize and initialize some data. We also have our own class loading system, this means you can save most classes without any modification. However, more complex classes may have issues if they aren't designed to be stored.

# Features 
- Simple to use java interface
- Supports automatic unloading of inactive objects
- Supports automatic saving at regular intervals + manual saves
- Uses a buffered file reader, only reads 256 bytes at a time
- Automatic class loading*
- Automatically serializes and loads most classes


**Can fail if the library is called in unregular class loader conditions*

# Getting Started

To get started, you must first import Xilla Boot to your java project. 

**Maven**
Add instructions when repo is online

**Gradle**
Add instructions when repo is online

If your program is stand-alone this configuration alone is fine. However, if you are running this library alongside other applications/plugins that may use this framework as well, you may with to shadow this library into your project and relocate the framework within your own project to prevent any issues.

You can do this by adding this to your maven configuration 

```
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-shade-plugin</artifactId>
    <version>3.2.4</version>
    <executions>
        <execution>
            <phase>package</phase>
            <goals>
                <goal>shade</goal>
            </goals>
            <configuration>
                <relocations>
                    <relocation>
                        <pattern>net.xilla.boot</pattern>
                        <shadedPattern>your.package.boot</shadedPattern>
                    </relocation>
                </relocations>
            </configuration>
        </execution>
    </executions>
</plugin>
```

Once your done with the above, you will need to initialize your application. The name is required for future logging tooling, however feel free to just put a blank string if you'd like.

```
XillaApplication.create(String name); // or optionally to disable automatic class loading, use the one below
XillaApplication.create(String name, boolean reflection)

// You can automatically flag one of your classes as needing to be stored and access it later.
// Add one of these annotations to your class file
@CacheManager // Stores everything in ram, no autosaving or unloading
@JsonManager(fileName = "data.json") // Stores everything in a json file, will create a singular database file
@JsonFolderManager(folderName = "data/") // Stores everything in seperate json files

```

If you have class loading disabled, please read the following.
```
// If you disable reflection, now is the time to register your data managers. The rest of the code block is for those who disable class-loading
XillaApplication.getInstance().registerManager(Manager manager)

// To create a manager, you can run the following
new Manager(YourClass.class, DataLoader loader)

// There are a few default options for data managers, you can however add your own
new CacheLoader() // Stores everything in ram, no autosaving or unloading
new JsonLoader("data.json")) // Stores everything in a json file, will create a singular database file
new JsonFolderLoader("data/")) // Stores everything in seperate json files
```

Then you can register additional startup items if you have things that need to happen after the data is finished loading. 
```
XillaApplication.getInstance().registerStartupProcess(new StartupProcess(String name, StartPriority priority) {
    @Override
    public void run() {
        // Code here
    }
});
```

After thats all done, you can start the application with
`XillaApplication.getInstance().startup();`

Please keep in mind that any classes you store must have a valid identifier that is consistent! We automatically check for the methods `getId()`, `getID()`, `getName()`, and as a last resort `toString()` to generate the key for an object. This key is used to later access and modify the object.

# How to access and store data

I will add more to this later

```
YourClass object = XillaAPI.getObject(String key);
XillaAPI.setObject(YourClass.class, YourClass object);
XillaAPI.getManager(YourClass.class).save();
```
