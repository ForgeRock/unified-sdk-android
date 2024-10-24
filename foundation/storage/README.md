<div>
  <picture>
     <img src="https://www.pingidentity.com/content/dam/ping-6-2-assets/topnav-json-configs/Ping-Logo.svg" width="80" height="80"  alt=""/>
  </picture>
</div>

# Ping Storage SDK

The Ping Storage SDK provides a flexible storage interface and a set of common storage solutions for the Ping SDKs.

## Integrating the SDK into your project

To add the Ping Storage SDK as a dependency to your project, include the following in your `build.gradle` file:

```kotlin
dependencies {
    implementation(project(":foundation:storage"))
}
```

## How to Use the SDK

### Creating and Using a Storage Instance

To create a storage instance and use it to persist and retrieve data, follow the example below:

```kotlin
// Define the data class that you want to persist
@Serializable
data class Dog(val name: String, val type: String)

val storage = EncryptedStorage<Dog>("myId") // Create the Storage
storage.save(Dog("Lucky", "Golden Retriever")) // Persist the data object

val storedData = storage.get() // Retrieve the object
```

EncryptedStorage is a storage solution that
uses [EncryptedSharedPreferences](https://developer.android.com/reference/androidx/security/crypto/EncryptedSharedPreferences)
to store data securely.

### Enabling Cache for the Storage

You can enable cache for the storage as follows, by default cache is disabled:

```kotlin
val storage = EncryptedStorage<Dog>("myId", cacheable = true) // Create the Storage with cache enabled
```

### DataStoreStorage

DataStoreStorage uses [DataStore](https://developer.android.com/topic/libraries/architecture/datastore) to store data.

```kotlin
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
val storage = DataStoreStorage<Dog>(context.dataStore)
```

### Creating a Custom Storage

You can create a custom repository by implementing the `Repository` interface. This could be useful for creating
file-based storage, cloud storage, etc. Here is an example of creating a custom memory storage:

```kotlin
class MemoryRepository<T : @Serializable Any> : Repository<T> {
    private var data: T? = null

    override suspend fun save(item: T?) {
        data = item
    }

    override suspend fun get(): T? = data

    override suspend fun delete() {
        data = null
    }
}

// Delegate the MemoryRepository to the Storage
inline fun <reified T : @Serializable Any> MemoryStorage(): Storage<T> = Storage(MemoryRepository())
```

## Available Storage Solutions

The Ping Storage SDK provides the following storage solutions:

| Storage          | Description                                                                                                                                 |
|------------------|---------------------------------------------------------------------------------------------------------------------------------------------|
| EncryptedStorage | Storage backed by [EncryptedSharedPreferences](https://developer.android.com/reference/androidx/security/crypto/EncryptedSharedPreferences) |
| DataStoreStorage | Storage that store data in  [DataStore](https://developer.android.com/topic/libraries/architecture/datastore).                              |
| MemoryStorage    | Storage that store data in memory.                                                                                                          |
