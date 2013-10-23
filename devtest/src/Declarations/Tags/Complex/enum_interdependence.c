//##################################################################
// Intra/inter enumerate dependence
//##################################################################


// Named
//-------

// Refers to itself
enum E0 {enum0_D, enum0_E, enum0_F=enum0_D} e0;


// Refers to E1
enum E1 {enum1_A, enum1_B = 3, enum1_C};
enum E  {A=enum1_B, B, C} e;


// Refers to E1 and E2
enum E2   {enum2_A, enum2_B, enum2_C};
enum E3 {enum3_D=enum1_A+enum2_B, enum3_E, enum3_F} ebis;




// Unnamed
//---------

// Refers to itself
enum {noname_D, noname_E, noname_F=noname_D};


// Refers to previous
enum {noname1_A, noname1_B = 3, noname1_C};
enum {noname_A=noname1_B, noname_B, noname_C};


// Refers to 2 fi
enum {noname2_A, noname2_B, noname2_C};
enum {noname3_D=noname1_A+noname2_B, noname3_E, noname3_F};




