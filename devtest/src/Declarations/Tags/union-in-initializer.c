//##################################################################
// Union declared in an initializer
//##################################################################

// Named
int a=sizeof(union U {int i; char toto;});

// Unnamed
int b=sizeof(union {int i; char toto;});


union U u; // Correct reference to complete tag

