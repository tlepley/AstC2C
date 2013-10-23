//
// The return type is not compatible for assignment
//

struct S {int i;};
int f() {
  struct S s;
  return s;
}
