// In C types can be declared in the parameter list, even if it is not
// really meaningfull


//=====================
// Function prototype
//=====================

// Named struct
void pf1(struct S {int i;} * a);
// Anonymous struct
void pf2(struct {int i;} * a);

// Named union
void pf3(union S {int i;} * a);
// Anonymous union
void pf4(union {int i;} * a);

// Named enum
void pf5(enum E {A} * a);
// Anonymous enum
void pf6(enum {A} * a);



//=====================
// Function definition
//=====================

// Named struct
void f1(struct S {int i;} * a) {}
// Anonymous struct
void f2(struct {int i;} * a) {}

// Named union
void f3(union S {int i;} * a) {}
// Anonymous union
void f4(union {int i;} * a) {}

// Named enum
void f5(enum E {A} * a) {}
// Anonymous enum
void f6(enum {A} * a) {}


//===========================
// Propagation of information
//===========================
// Both S and S2 must raise a warning
void f(struct S {struct S2 {int i;} i;} * i);


