//
// Test pointer to functions into type tags
//


// Extracted (instance data)
struct T {
  int (*f)(int i);
  int (*g)(void);
  int (*h)();
} a;

// Non extracted
struct S {
  int (*f)(int i);
  int (*g)(void);
  int (*h)();
};


void f(struct S *s) {
  s->f(2);
  s->g(1);
}
