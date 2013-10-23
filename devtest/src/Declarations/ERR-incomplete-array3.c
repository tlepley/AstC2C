// Incomplete array, should generate an error

int f() {
  int ((*p)[])[];
}
