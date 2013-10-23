// A structure variable can be initialized by an other structure variable, not only by
// a compund initializer

struct S {int i; int j;} s1;

int f() {
  struct S s2=s1;
}
