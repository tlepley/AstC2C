// The second struct just reference the first one since they ar ein the same scope. It does not declare
// a new uncomplete structure

int f() {
  struct S {int i;};
  struct S;
  struct S s;
  return s.i;
}
