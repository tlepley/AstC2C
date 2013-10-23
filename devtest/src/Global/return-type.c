// Compatibility between the returned function type and the type of the 'return'
// statement


// Simple return type
int a;
int f1() {
  return a;
}
int *f2() {
  return &a;
}

// Aggregate return type
struct S {int i;} s;
struct S f3() {
  return s;
}
union U {int i;} u;
union U f4() {
  return u;
}
enum E {A, B} e;
enum E f5() {
  return e;
}
enum E f6() {
  return A;
}


// More complex return type (pointer to function)
struct S g();
struct S (*f7())() {
  return g;
}

