# CSCI4180 Assignment 3

## Classes
- `MyDedup` - Main Program
- `Backend` - Interface for different storage platforms
- `Local` - Implementation of `Backend` for Local Storage
- `Azure` - Implementation of `Backend` for Microsoft Azure Storage
- `S3` - Implementation of `Backend` for Amazon S3 Storage
- `Index` - Definition of `MyDedup.index` format
- `IndexReader` - Reader for the content of `MyDedup.index`

## Makefile
### Main Program
```
make MyDedup
```
### Index File Reader
```
make IndexReader
```


## Usage
### Main Program
```
java -cp .:./lib/* MyDedup upload min_chunk avg_chunk max_chunk d file_to_upload <local|azure|s3>
java -cp .:./lib/* MyDedup download file_to_download <local|azure|s3>
java -cp .:./lib/* MyDedup delete file_to_delete <local|azure|s3>
```
### Index File Reader
```
java IndexReader <path_of_MyDedup.index>
```