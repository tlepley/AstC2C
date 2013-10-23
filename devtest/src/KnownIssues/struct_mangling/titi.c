//
// Assign to local structure, a structure which is defined in
// an other file
// => Mangling of the structure are not the same, which ends up to
//    a compilation error
//

struct S {int i;} s1;

extern struct S s2;

int f() {
  int a;
  static struct S s_static;
  struct S s_local;

  s_local=s2;
  s_static=s2;
}

