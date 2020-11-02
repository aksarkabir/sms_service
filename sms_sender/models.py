from django.db import models
from django import forms
from django.forms import ModelForm,PasswordInput,TextInput
from django.contrib.auth.forms import UserCreationForm
from django.contrib.auth.models import User
# Create your models here.

class registration_form(UserCreationForm):
    password1 = forms.CharField(max_length=1000, widget=forms.PasswordInput(attrs={'class': 'w3-border'}))
    password2 = forms.CharField(max_length=1000, widget=forms.PasswordInput(attrs={'class': 'w3-border'}))
    class Meta:
        model = User
        fields = ['username','password1','password2']
        widgets = {
            'username':TextInput(
                attrs = {'class':'w3-border'}
            ),
        }

class phone_book_list(models.Model):
    user_name = models.ForeignKey(User,on_delete=models.CASCADE)
    phonebook_name = models.CharField(max_length=100)

    def __str__(self):
        return self.phonebook_name

class group_name(models.Model):
    phonebook = models.ForeignKey(phone_book_list,on_delete=models.CASCADE)
    name = models.CharField(max_length=100)
    def __str__(self):
        return self.name

class phone_book(models.Model):
    phonebook = models.ForeignKey(phone_book_list,on_delete=models.CASCADE)
    group = models.ManyToManyField(group_name,null = True,blank=True)
    number = models.CharField(max_length=30)
    name = models.CharField(max_length=250)
    user_name = models.ForeignKey(User,on_delete = models.CASCADE,null = True)

    def __str__(self):
        return self.name

class sms(models.Model):
    user_name = models.ForeignKey(User,on_delete=models.CASCADE)
    phone = models.ForeignKey(phone_book,on_delete = models.SET_NULL,null = True)
    number = models.CharField(max_length=30)
    message = models.CharField(max_length=2000,null = True, blank = True)
    schadule = models.CharField(max_length=20,null = True, blank = True)
    sms_status = models.CharField(max_length=20,null=True)
    date = models.CharField(max_length=30,null = True)
    readornot = models.CharField(max_length=10, null = True)

    def __str__(self):
        return self.message

class device_info(models.Model):
    user_name =models.ForeignKey(User,on_delete=models.CASCADE)
    name = models.CharField(max_length=200,null=True)
    active = models.CharField(max_length=15,null=True)
    brand = models.CharField(max_length=100,null=True)
    model = models.CharField(max_length=100,null = True)
    id_phone = models.CharField(max_length=100,null = True)

    def __str__(self):
        return self.name

class date_info(models.Model):
    user_name = models.ForeignKey(User,on_delete = models.CASCADE)
    date = models.CharField(max_length=30,null = True)

    def __str__(self):
        return self.date
