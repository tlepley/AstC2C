//##################################################################
// Enum specifier
//
// Test simple enum tag and enum fields declarations
//##################################################################


// Named complete enum
enum E1 {A1, B1};
enum E2 {A2=10, B2=15}; // Field setting
enum E3 {A3} e1;	    // Variable
enum E4 {A4=4, B4, C4} e2; // Variable, Field setting
typedef enum E5 {A5, B5=3, C5} t; // typedef

// Unnamed complete enum
enum {UA1, UB1};
enum {UA2=10, UB2=15}; // Field setting
enum {UA3} ue1;	    // Variable
enum {UA4=4, UB4, UC4} ue2; // Variable, Field setting
typedef enum {UA5, UB5=3, UC5} ut; // typedef

// Uncomplete enum
enum EE *c;


// Reference to an enum
enum E1 r1;
enum E3 r2;
