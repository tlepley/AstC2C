//
// Mix instance variables which are initialized and not initialized
//

int i;

char *b="hello world";

int j=0;

struct S1 {int i; int j;} s1;

int main() {
  struct S2 {char a; int b;};
  static struct S2 s2 = {'a',2};
}


char c;
