//##################################################################
// Enum declared in an initializer
//##################################################################


// Named
int a=sizeof(enum E {A,B,C});

// Unnamed
int b=sizeof(enum {A1,B1,C1});


// Correct references
enum E e=A;
int i=B1;
