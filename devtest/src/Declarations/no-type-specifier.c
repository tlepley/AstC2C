//
// Test declarations without type specifier
//


// 'a' is an integer so the regenerated code must be 'int a'
a;

// 'b' is an integer so the regenerated code must be 'static int a'
static b;

// Returns an integer so the regenerated code must be 'int f()'
f();

// Returns an integer so the regenerated code must be 'inline int f1()'
inline f1();

// Returns an integer so the regenerated code must be 'int g() {}'
g() {}

// Returns an integer so the regenerated code must be 'static int g1() {}'
static g1() {}

// This is a declaration without initDecl
typedef t;
t;

