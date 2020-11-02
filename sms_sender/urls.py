from django.conf.urls import url,include
from . import views
from rest_framework import routers

app_name = 'sms'

urlpatterns = [
    url(r'^registration/$',views.registration, name = 'registration'),
    url(r'^index/$',views.index,name = 'index'),
    url(r'^logout/$',views.logout_view,name='logout'),
    url(r'^save-4566-the-65de-number/$',views.save_number,name='savenumber'),
    url(r'^phone-book-for-the-user/$',views.phoneBook,name = 'phonebook'),
    url(r'^add-phone-book-for-the-user/$',views.phoneBookname,name = 'phonebookname'),
    url(r'^add-group/$',views.save_group,name = 'save_group'),
    url(r'^group(?P<pk_group>[0-9]+)adas/$',views.phonebook_group,name = 'group'),
    url(r'^add-group-to-number/$',views.add_group,name = 'add_group'),
    url(r'^delete-group-for-this-number/$',views.delete_group,name='delete_group'),
    url(r'^delete-number(?P<id_please>[0-9]+)-are-u-sure/$',views.delete_number,name='delete_number'),
    url(r'^deletepho(?P<id_please>[0-9]+)nebookplease/$',views.delete_phonebook,name = 'delete_phonebook'),
    url(r'^write-your-sms-here-please-6789/$',views.write_sms,name = 'write_sms'),
    url(r'^send-sms-please/$',views.save_sms,name = 'save_sms'),
    url(r'^api-data/$',views.get_api, name ='api'),
    url(r'^all-the-devices/$',views.devices,name='device'),
    url(r'^sms-details/$',views.sms_view,name = 'sms_view'),
    url(r'^get_phonebook/$',views.get_phonebook, name = 'get_phonebook'),
    url(r'^save_number_from_phone/$',views.save_number_from_phone, name = 'save_number_from_phone'),
    url(r'^save_phonebook_from_phone/$',views.savephonebookfromphone,name = 'savephonebookfromphone'),
    url(r'^get-unsend-msg-count/$',views.getunsendmegcount,name = 'getunsendmsgcount'),
    url(r'^get-unsend-msg/$',views.getunsendmsg,name = 'getunsendmsg'),
    url(r'^sms-status-save/$',views.save_status_sms, name = 'sms_status_save'),
    url(r'^get-suggetion-for-num/$', views.get_suggetions, name='get_suggetion'),
    url(r'^add-number-to-group-search/$',views.get_group_search,name = 'get_group_search'),
    url(r'^make-active/$',views.make_active,name='make_active'),
    url(r'^get-sms-from-phone/$',views.received_sms,name = 'received_sms'),
]
