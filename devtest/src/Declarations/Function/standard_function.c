//
// Test 'this' insertion
//

// Definition
f1() {;}
int f2() {;}
void f3(int i) {;}
int f4(char c, int i) {;}

int main() {
  f1();
  f2();
  f3(1);
  return(f4('a',2));
}
