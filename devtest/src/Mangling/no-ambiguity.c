#pragma ast_mangling on
void f(int i, unsigned int j);
void f(float i, unsigned int j);
#pragma ast_mangling off


void main() {
  // Both arguments are signed int, but since only one conversion needed in the first 'f', there is
  // No ambiguity
  f(1,2);
}
