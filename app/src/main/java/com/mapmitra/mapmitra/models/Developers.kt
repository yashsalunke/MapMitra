package com.mapmitra.mapmitra.models

import com.google.firebase.firestore.PropertyName

class Developers(
    @get:PropertyName("Name") @set:PropertyName("Name") var name: String? = null ,
    @get:PropertyName("E-mail") @set:PropertyName("E-mail") var Email: String? = null ,
    @get:PropertyName("Contact Number") @set:PropertyName("Contact Number") var contact_number: Long? = null ,
    @get:PropertyName("Linked-In Profile") @set:PropertyName("Linked-In Profile") var linkedIn: String? = null ,
    @get:PropertyName("Image") @set:PropertyName("Image") var image_url: String? = null
)