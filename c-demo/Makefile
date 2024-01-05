.PHONY: all clean

all: demo

clean:
	rm demo demo.o

demo: demo.o
	gcc -o $@ $< -lpng

demo.o: demo.c
	gcc -c -o $@ $<
