//
// Test of complex function declarators with nested declarators
//


// f(int) returning a function(int, int) returning an int
int (*f1(int i))(int i, int k);
int (*f1(int i))(int i, int k) {}

// f(int) returning a pointer to an array of 10 elements containg an array
// of pointers to a function(int, int) returning an int
int (*(* f3(int i))[10])(int i, int k);
int (*(* f3(int i))[10])(int i, int k) {}

