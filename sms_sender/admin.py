from django.contrib import admin
from .models import phone_book_list,group_name,phone_book,sms,device_info,date_info
# Register your models here.

admin.site.register(phone_book)
admin.site.register(group_name)
admin.site.register(phone_book_list)
admin.site.register(sms)
admin.site.register(device_info)
admin.site.register(date_info)
