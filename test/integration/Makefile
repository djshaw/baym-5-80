.PHONY: default
default: run

.PHONY: classes
classes:
	javac -cp junit-platform-console-standalone-1.5.2.jar:junit-platform-runner-1.5.2.jar:. *.java

.PHONY: test
test: classes
	java -jar junit-platform-console-standalone-1.5.2.jar --class-path . --include-classname=.* --scan-classpath --disable-banner

.PHONY: run
run: classes test
	./run

.PHONE: clean
clean:
	rm -f *.class

