//##################################################################
// 'escape' reference to global scope
//##################################################################

// 'a' in the printf references the global variable and not the
// local variable 
// The 'extern' declaration is kept in the compound statement to keep
// the semantics of the program.

void printf(char *,...);
int a=2;

int main() {
  int a=1;

  if (a) {	       // a=1
    extern int a;
    printf("%d\n",a);  // a=2
  }
}
