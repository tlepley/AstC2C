//
// Test of complex function declarators, with several parameter list and
// array at the same level
//
// Note: These tests where a function returns a function should not pass
// complete correctly, but since such check is not done yet, it works


// f(int) returning a function(int, int) returning an int
// Note: both i must not enter in conflict
int (*f1(int i))(int i, int k);
int (*f2(int i))(int i, int k) {}


// f(int) returning a array of 10 elements containg a function(int, int)
// returning an int
int (*(*(*f3)(int i))[10])(int i, int k);
int (*(*f4(int i))[10])(int i, int k) {}

