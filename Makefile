MyDedup: MyDedup.java Backend.java Local.java Index.java
	javac -cp .:lib/* MyDedup.java Backend.java Local.java Azure.java Index.java S3.java

IndexReader: Index.java IndexReader.java
	javac Index.java IndexReader.java

clean:
	rm *.class