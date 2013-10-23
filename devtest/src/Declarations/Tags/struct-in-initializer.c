//##################################################################
// Structure declared in an initializer
//##################################################################

// Named
int a=sizeof(struct S {int i; int toto;});

// Unnamed
int b=sizeof(struct {int i; int toto;});


struct S s; // Correct reference to complete tag
