from rest_framework import serializers
from .models import phone_book_list,sms,phone_book

class get_phonebook_model(serializers.ModelSerializer):
    class Meta:
        model = phone_book_list
        fields = ['phonebook_name']

class get_unsend_msg(serializers.ModelSerializer):
    class Meta:
        model = sms
        fields = ['user_name','phone','number','message','sms_status','id']

class get_hint(serializers.ModelSerializer):
    class Meta:
        model = phone_book
        fields = ['number','name','id']
