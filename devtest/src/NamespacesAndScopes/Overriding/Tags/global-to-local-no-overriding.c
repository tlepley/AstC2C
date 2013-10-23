//
// There is here no overriding. Simply declare a local variable from a global
// structure declaration
//

struct S {int i;};

int fs() {
  struct S s;
  return s.i;
}


union U {int i;struct S s;};

struct S fu() {
  union U u;
  return u.s;
}


