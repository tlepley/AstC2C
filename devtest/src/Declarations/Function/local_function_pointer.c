//
// Test if local pointer to function is correctly handled 
// with the 'this'
//

void f() {
  int (*is_ref)(int *s);

  if(is_ref(1)) {
    ;
  }
}
