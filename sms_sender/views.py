from django.shortcuts import render,redirect
from django.contrib.auth import authenticate,login,logout
from django.contrib.auth.models import User
from .models import registration_form,phone_book_list,group_name,phone_book,sms,device_info,date_info
from datetime import date
from django.urls import reverse
from django.db.models import Q
from django.views.generic.edit import CreateView,DeleteView
from rest_framework import viewsets
from rest_framework.response import Response
from rest_framework.decorators import api_view
from .serial import get_phonebook_model,get_unsend_msg,get_hint
import json
import datetime
# from snippets.serializers import SnippetSerializer
# Create your views here.

def registration(request):
    if request.method == 'POST':
        form = registration_form(request.POST or None)
        if form.is_valid():
            form.save()
            return redirect("sms:index")
        else:
            render(request,'sms/reg.html',{'form':form})
    else:
        form = registration_form()
    context = {
        'form':form
    }
    return render(request,'sms/reg.html',context)

def index(request):
    dt = datetime.datetime.now()
    x = str(dt.day)+'-'+str(dt.month)+'-'+str(dt.year)
    request.session['group_modal']='none'
    request.session['p_name']=''
    # date_today = date.today()
    if request.user.is_authenticated:
        if not date_info.objects.filter(user_name=User.objects.get(username=request.user.username),date=x).exists():
            dateinfo = date_info()
            dateinfo.user_name = User.objects.get(username=request.user.username)
            dateinfo.date = x
            dateinfo.save()
        if request.method == 'POST':
            if request.POST['form_name']=='select_date':
                total_sms = sms.objects.filter(user_name = User.objects.get(username=request.user.username),date=request.POST['date_list']).count()
                send_sms = sms.objects.filter(user_name = User.objects.get(username=request.user.username),sms_status='yes',date=request.POST['date_list']).count()
                not_send_sms = sms.objects.filter(user_name = User.objects.get(username=request.user.username),sms_status='no',date=request.POST['date_list']).count()
                received_sms = sms.objects.filter(user_name = User.objects.get(username=request.user.username),sms_status='received',date=request.POST['date_list']).count()
                date = date_info.objects.filter(user_name = User.objects.get(username=request.user.username)).order_by('-id')
                context = {
                    'user':request.user,
                    'total_sms':total_sms,
                    'send_sms':send_sms,
                    'not_send_sms':not_send_sms,
                    'received_sms':received_sms,
                    'dates':date,
                    'current_date':request.POST['date_list']
                }
                return render(request,'sms/details.html',context)
        total_sms = sms.objects.filter(user_name = User.objects.get(username=request.user.username),date=x).count()
        send_sms = sms.objects.filter(user_name = User.objects.get(username=request.user.username),sms_status='yes',date=x).count()
        not_send_sms = sms.objects.filter(user_name = User.objects.get(username=request.user.username),sms_status='no',date=x).count()
        received_sms = sms.objects.filter(user_name = User.objects.get(username=request.user.username),sms_status='received',date=x).count()
        date = date_info.objects.filter(user_name = User.objects.get(username=request.user.username)).order_by('-id')
        context = {
            'user':request.user,
            'total_sms':total_sms,
            'send_sms':send_sms,
            'not_send_sms':not_send_sms,
            'received_sms':received_sms,
            'dates':date,
            'current_date':x
        }
        return render(request,'sms/details.html',context)
    if request.method == 'POST':
        username = request.POST['username']
        password = request.POST['password']
        user = authenticate(request,username = username,password = password)
        total_sms = sms.objects.filter(user_name = User.objects.get(username=request.POST['username']),date=x).count()
        send_sms = sms.objects.filter(user_name = User.objects.get(username=request.POST['username']),sms_status='yes',date=x).count()
        not_send_sms = sms.objects.filter(user_name = User.objects.get(username=request.POST['username']),sms_status='no',date=x).count()
        received_sms = sms.objects.filter(user_name = User.objects.get(username=request.POST['username']),sms_status='received',date=x).count()
        date = date_info.objects.filter(user_name = User.objects.get(username=request.POST['username'])).order_by('-id')
        if user is not None:
            login(request,user)
            if not date_info.objects.filter(user_name=User.objects.get(username=request.POST['username']),date=x).exists():
                dateinfo = date_info()
                dateinfo.user_name = User.objects.get(username=request.POST['username'])
                dateinfo.date = x
                dateinfo.save()
            context = {
            'user':user,
            'total_sms':total_sms,
            'send_sms':send_sms,
            'not_send_sms':not_send_sms,
            'received_sms':received_sms,
            'dates':date,
            'current_date':x
            }
            return render(request,'sms/details.html',context)
        else:
            user_value = 'not'
            context = {
                'user':user_value,
            }
            return render(request,'sms/index.html',context)
    return render(request,'sms/index.html',{})


def logout_view(request):
    logout(request)
    return redirect('sms:index')

def save_number(request):
    pb = phone_book()
    pb.name = request.POST['name_number']
    pb.number = request.POST['number']
    pbn = phone_book_list.objects.get(id = int(request.POST['phonebook']))
    pb.phonebook = pbn
    pb.user_name = User.objects.get(username = request.user.username)
    pb.save()
    return redirect('sms:phonebook')

def phoneBook(request):
    request.session['group_modal']='none'
    if request.user.is_authenticated:
        if request.method == 'POST':
            if request.POST['fn']=='delete_group_name':
                group_id = group_name.objects.get(id = request.POST['group_id'])
                group_id.delete()
                return redirect('sms:phonebook')
            p_name =int(request.POST['phonebook_name'])
            number = phone_book.objects.filter(phonebook=p_name).order_by('name')
            phonebook_name = phone_book_list.objects.get(id = p_name)
            phonebook = phone_book_list.objects.filter(user_name = request.user).order_by('phonebook_name')
            group = phone_book_list.objects.get(id = p_name)
            request.session['p_name']=p_name
        else:
            phonebook = phone_book_list.objects.filter(user_name = request.user).order_by('phonebook_name')
            if len(phonebook)!=0:
                if request.session['p_name']!='':
                    p_name = request.session['p_name']
                    print(p_name)
                else:
                    p_name = phonebook[0].id
                    # print(p_name)

                request.session['p_name']=p_name
                number = phone_book.objects.filter(phonebook=p_name).order_by('name')
                group = phone_book_list.objects.get(id = int(p_name))
                phonebook_name = phone_book_list.objects.get(id = int(p_name))
                # group = ''
                # phonebook_name = ''
            else:
                p_name = ''
                group = ''
                phonebook_name=''
                number = ''
                request.session['p_name']=''
        context = {
            'user': request.user,
            'phonebook':phonebook,
            'p_name':p_name,
            'group':group,
            'phonebook_name':phonebook_name,
            'number':number,
        }
        return render(request,'sms/phonebook.html',context)
    return redirect('sms:index')

def phoneBookname(request):
    user_name = User.objects.get(username = request.user.username)
    pb = phone_book_list()
    pb.user_name = user_name
    pb.phonebook_name = request.POST['phonebook_name']
    pb.save()

    return redirect('sms:phonebook')

def save_group(request):
    gp = group_name()
    gp.phonebook = phone_book_list.objects.get(id = int(request.POST['phonebook']))
    gp.name = request.POST['name']
    gp.save()
    return redirect('sms:phonebook')

def phonebook_group(request,pk_group):
    m = request.session['group_modal']
    group = group_name.objects.get(id = pk_group)
    number = phone_book.objects.filter(~Q(group=group.id),phonebook = group.phonebook.id).order_by('name')
    context = {
        'group':group,
        'number':number,
        'm':m
    }
    return render(request,'sms/group.html',context)

def add_group(request):
    number = int(request.POST['id'])
    group = int(request.POST['group'])
    nm = phone_book.objects.get(id = number)
    nm.group.add(group_name.objects.get(id = group))
    if request.POST['form_name']!='no_model':
        request.session['group_modal']='block'
    else:
        request.session['group_modal']='none'
    return redirect('sms:group',group)

def delete_group(request):
    number = int(request.POST['id'])
    group = int(request.POST['group'])
    nm = phone_book.objects.get(id = number)
    nm.group.remove(group_name.objects.get(id = group))
    request.session['group_modal']='none'
    return redirect('sms:group',group)

def delete_number(request,id_please):
    number = phone_book.objects.get(id = id_please)
    if request.method=='POST':
        number = phone_book.objects.get(id = int(request.POST['number']))
        number.delete()
        return redirect('sms:phonebook')
    return render(request,'sms/delete_number.html',{'number':number})

def delete_phonebook(request,id_please):
    phonebook = phone_book_list.objects.get(id = id_please)
    request.session['p_name']=''
    if request.method == 'POST':
        id = phone_book_list.objects.get(id =int(id_please))
        id.delete()
        return redirect('sms:phonebook')
    return render(request,'sms/delete_phonebook.html',{'phonebook':phonebook})

def write_sms(request):

    if request.method == 'POST':
        group = request.POST['group_or_not']
        if group == 'no':
            id = int(request.POST['phone'])
            number = phone_book.objects.get(id = id)
        else:
            number = group_name.objects.get(id = int(request.POST['group_id']))
        # if the group is yes the it will take the group id
        context = {
            'group':group,
            'number':number,
        }

        return render(request,'sms/sms_send.html',context)

def save_sms(request):
    group = request.POST['group']
    dt = datetime.datetime.now()
    x = str(dt.day)+'-'+str(dt.month)+'-'+str(dt.year)
    if not date_info.objects.filter(date=x,user_name=User.objects.get(username=request.user.username)).exists():
        dateinfo = date_info()
        dateinfo.user_name = User.objects.get(username = request.user.username)
        dateinfo.date = x
        dateinfo.save()
    if group == 'no':
        snd = sms()
        pp = request.POST['phone']
        snd.user_name = User.objects.get(username = request.user.username)
        snd.phone = phone_book.objects.get(id = pp)
        # print(pp)
        snd.number = request.POST['number']
        snd.message = request.POST['message']
        snd.sms_status = 'no'
        snd.date = x
        snd.save()
        print(x)
    elif group == 'yes':
        str_list=request.POST['number'].split(' ')
        str_list = list(filter(None, str_list))
        print(str_list)
        for nm in str_list:
            snd = sms()
            number = phone_book.objects.get(id = int(nm))
            snd.user_name = User.objects.get(username = request.user.username)
            snd.phone = number
            snd.number = number.number
            snd.message = request.POST['message']
            snd.sms_status = 'no'
            snd.date = x
            snd.save()
    return redirect('sms:phonebook')

# @csrf_exempt
# @api_view(["POST"])
# @permission_classes((AllowAny,))
@api_view(['GET', 'POST'])

def get_api(request):
    c = request.POST['id']
    d = request.POST['pass']
    user = authenticate(request,username = c,password = d)
    if user is not None:
        if not device_info.objects.filter(name=request.POST['device_name'],user_name=User.objects.get(username=c)).exists():
            device = device_info()
            device.user_name = User.objects.get(username = c)
            device.name = request.POST['device_name']
            device.active = 'no'
            device.brand = request.POST['brand']
            device.model = request.POST['model']
            device.id_phone = request.POST['id_phone']
            device.save()

        a = {
            'reply':'yes'
        }
    else:
        a = {
            'reply':'no'
        }
    return Response(a)
    # s = SnippetSerializer(a,many=True)

def devices(request):
    if request.user.is_authenticated:
        if request.method=='POST':
            device = device_info.objects.get(id=int(request.POST['id']))
            context = {
            'device':device,
            }
            return render(request,'sms/device_details.html',context)
        device = device_info.objects.filter(user_name=User.objects.get(username=request.user.username))
        active = device_info.objects.filter(user_name=User.objects.get(username=request.user.username),active = 'yes')
        context = {
            'device':device,
            'active':active,
        }
        return render(request,'sms/devices.html',context)
    else:
        return redirect('sms:index')

def sms_view(request):
    dt = datetime.datetime.now()
    x = str(dt.day)+'-'+str(dt.month)+'-'+str(dt.year)
    if request.user.is_authenticated:
        if request.method=='POST':
            if request.POST['form_name']=='new':
                active_new='w3-blue'
                active = 'new'
                context = {
                    'active_new':active_new,
                    'active':active,
                }
                return render(request,'sms/sms_view.html',context)
            elif request.POST['form_name']=='send':
                active_send='w3-blue'
                active = 'send'
                date = date_info.objects.filter(user_name = User.objects.get(username=request.user.username)).order_by('-id')
                if sms.objects.filter(sms_status='yes',user_name= User.objects.get(username=request.user.username),date = x).exists():
                    send = sms.objects.filter(sms_status='yes',user_name= User.objects.get(username=request.user.username),date = x).order_by('-id')
                else:
                    send = ''
                context = {
                    'active_send':active_send,
                    'active':active,
                    'send':send,
                    'dates':date,
                    'current_date':x
                }
                return render(request,'sms/sms_view.html',context)
            elif request.POST['form_name']=='not':
                active_not='w3-blue'
                active = 'not'
                date = date_info.objects.filter(user_name = User.objects.get(username=request.user.username)).order_by('-id')
                if sms.objects.filter(sms_status='no',user_name= User.objects.get(username=request.user.username),date = x).exists():
                    not_send = sms.objects.filter(sms_status='no',user_name= User.objects.get(username=request.user.username),date=x)
                else:
                    not_send = ''
                context = {
                    'active_not':active_not,
                    'active':active,
                    'not_send':not_send,
                    'dates':date,
                    'current_date':x
                }
                return render(request,'sms/sms_view.html',context)
            elif request.POST['form_name']=='write':
                if not date_info.objects.filter(date=x,user_name=User.objects.get(username=request.user.username)).exists():
                    dateinfo = date_info()
                    dateinfo.user_name = User.objects.get(username = request.user.username)
                    dateinfo.date = x
                    dateinfo.save()
                if phone_book.objects.filter(number__contains = request.POST['number'],user_name = User.objects.get(username = request.user.username) ).exists():
                    snd = sms()
                    aaaaa = phone_book.objects.filter(number__contains= request.POST['number'],user_name=User.objects.get(username = request.user.username) )[0]
                    snd.phone = aaaaa
                    snd.user_name = User.objects.get(username = request.user.username)
                    snd.number = aaaaa.number
                    snd.message = request.POST['message']
                    snd.sms_status = 'no'
                    snd.date = x
                    snd.save()
                else:
                    snd = sms()
                    snd.user_name = User.objects.get(username = request.user.username)
                    snd.number = request.POST['number']
                    snd.message = request.POST['message']
                    snd.sms_status = 'no'
                    snd.date = x
                    snd.save()
            elif request.POST['form_name']== 'sms_details':
                not_details = sms.objects.get(id = request.POST['sms_id'])
                back = request.POST['back']
                context = {
                    'not_details':not_details,
                    'back':back
                }
                return render(request,'sms/sms_details.html',context)
            elif request.POST['form_name'] == 'sms_delete':
                back = request.POST['back']
                delete_details = sms.objects.get(id = request.POST['sms_id'])
                context = {
                    'delete_details':delete_details,
                    'back':back
                }
                return render(request,'sms/delete_sms.html',context)
            elif request.POST['form_name']=='delete_sms_forever':
                id = sms.objects.get(id = request.POST['sms_id'])
                id.delete()
                if request.POST['back'] == 'not':
                    active_not='w3-blue'
                    active = 'not'
                    date = date_info.objects.filter(user_name = User.objects.get(username=request.user.username)).order_by('-id')
                    if sms.objects.filter(sms_status='no',user_name= User.objects.get(username=request.user.username),date = request.POST['date_list']).exists():
                        not_send = sms.objects.filter(sms_status='no',user_name= User.objects.get(username=request.user.username),date=request.POST['date_list'])
                    else:
                        not_send = ''
                    context = {
                        'active_not':active_not,
                        'active':active,
                        'not_send':not_send,
                        'dates':date,
                        'current_date':request.POST['date_list']
                    }
                    return render(request,'sms/sms_view.html',context)
                elif request.POST['back'] == 'send':
                    active_send='w3-blue'
                    active = 'send'
                    date = date_info.objects.filter(user_name = User.objects.get(username=request.user.username)).order_by('-id')
                    if sms.objects.filter(sms_status='yes',user_name= User.objects.get(username=request.user.username),date = request.POST['date_list']).exists():
                        send = sms.objects.filter(sms_status='yes',user_name= User.objects.get(username=request.user.username),date = request.POST['date_list']).order_by('-id')
                    else:
                        send = ''
                    context = {
                        'active_send':active_send,
                        'active':active,
                        'send':send,
                        'dates':date,
                        'current_date':request.POST['date_list']
                    }
                    return render(request,'sms/sms_view.html',context)
                elif request.POST['back'] == 'rec':
                    active_rec='w3-blue'
                    active = 'rec'
                    date = date_info.objects.filter(user_name = User.objects.get(username=request.user.username)).order_by('-id')
                    if sms.objects.filter(sms_status='received',user_name= User.objects.get(username=request.user.username),date = request.POST['date_list']).exists():
                        rec = sms.objects.filter(sms_status='received',user_name= User.objects.get(username=request.user.username),date = request.POST['date_list']).order_by('-id')
                    else:
                        send = ''
                    context = {
                        'active_rec':active_rec,
                        'active':active,
                        'rec':rec,
                        'dates':date,
                        'current_date':request.POST['date_list']
                    }
                    return render(request,'sms/sms_view.html',context)
            elif request.POST['form_name'] == 'date_not':
                active_not='w3-blue'
                active = 'not'
                date = date_info.objects.filter(user_name = User.objects.get(username=request.user.username)).order_by('-id')
                if sms.objects.filter(sms_status='no',user_name= User.objects.get(username=request.user.username),date = request.POST['date_list']).exists():
                    not_send = sms.objects.filter(sms_status='no',user_name= User.objects.get(username=request.user.username),date=request.POST['date_list'])
                else:
                    not_send = ''
                context = {
                    'active_not':active_not,
                    'active':active,
                    'not_send':not_send,
                    'dates':date,
                    'current_date':request.POST['date_list']
                }
                return render(request,'sms/sms_view.html',context)
            elif request.POST['form_name'] == 'date_send':
                active_not='w3-blue'
                active = 'send'
                date = date_info.objects.filter(user_name = User.objects.get(username=request.user.username)).order_by('-id')
                if sms.objects.filter(sms_status='yes',user_name= User.objects.get(username=request.user.username),date = request.POST['date_list']).exists():
                    not_send = sms.objects.filter(sms_status='yes',user_name= User.objects.get(username=request.user.username),date=request.POST['date_list']).order_by('-id')
                else:
                    not_send = ''
                context = {
                    'active_send':active_not,
                    'active':active,
                    'send':not_send,
                    'dates':date,
                    'current_date':request.POST['date_list']
                }
                return render(request,'sms/sms_view.html',context)
            elif request.POST['form_name'] == 'received':
                active_rec = 'w3-blue'
                active = 'rec'
                date = date_info.objects.filter(user_name = User.objects.get(username=request.user.username)).order_by('-id')
                if sms.objects.filter(sms_status='received',user_name= User.objects.get(username=request.user.username),date = x).exists():
                    rec = sms.objects.filter(sms_status='received',user_name= User.objects.get(username=request.user.username),date = x).order_by('-id')
                else:
                    rec = ''
                context = {
                    'active_rec': active_rec,
                    'active': active,
                    'dates':date,
                    'current_date':x,
                    'rec':rec,
                }
                return render(request,'sms/sms_view.html',context)
            elif request.POST['form_name'] == 'date_rec':
                active_rec='w3-blue'
                active = 'rec'
                date = date_info.objects.filter(user_name = User.objects.get(username=request.user.username)).order_by('-id')
                if sms.objects.filter(sms_status='received',user_name= User.objects.get(username=request.user.username),date = request.POST['date_list']).exists():
                    rec = sms.objects.filter(sms_status='received',user_name= User.objects.get(username=request.user.username),date=request.POST['date_list']).order_by('-id')
                else:
                    rec = ''
                context = {
                    'active_rec':active_rec,
                    'active':active,
                    'rec':rec,
                    'dates':date,
                    'current_date':request.POST['date_list']
                }
                return render(request,'sms/sms_view.html',context)
        active_new = 'w3-blue'
        active = 'new'
        context = {
            'active_new':active_new,
            'active':active
        }
        return render(request,'sms/sms_view.html',context)
    else:
        return redirect('sms:index')
@api_view(['GET', 'POST'])
def get_phonebook(request):
    phonebook = phone_book_list.objects.filter(user_name=User.objects.get(username = request.POST['username']))
    srl = get_phonebook_model(phonebook,many = True)

    a = {
     'hi':'hi'
    }
    return Response(srl.data)
# to save number from the phone to server
# url = /sms/save_number_from_phone/
@api_view(['GET','POST'])

def save_number_from_phone(request):
    phonebook = request.POST['phonebook']
    number_details = json.loads(request.POST['number_details'])
    a = phone_book_list.objects.get(phonebook_name = request.POST['phonebook'],user_name=User.objects.get(username = request.POST['username']))
    for n in number_details:
        phonebook = phone_book()
        phonebook.phonebook = a
        phonebook.name = n['name']
        phonebook.number = n['number']
        phonebook.user_name = User.objects.get(username = request.POST['username'])
        phonebook.save()
    return Response({})

# url = sms/save_phonebook_from_phone/
# to save phonebook from phone
@api_view(['POST'])
def savephonebookfromphone(request):
    if phone_book_list.objects.filter(user_name = User.objects.get(username=request.POST['username']),phonebook_name = request.POST['phonebook_name']).exists():
        reply = {
            "reply":"already exists"
        }
        return Response(reply)
    else:
        phonebook = phone_book_list()
        phonebook.user_name = User.objects.get(username = request.POST['username'])
        phonebook.phonebook_name = request.POST['phonebook_name']
        phonebook.save()
        reply = {
            "reply":"success"
        }
        return Response(reply)
# url = sms/get-unsend-msg-count/
# to get all the unsend message number
@api_view(['POST'])
def getunsendmegcount(request):
    cn = sms.objects.filter(user_name = User.objects.get(username = request.POST['username']),sms_status='no').count()
    a = device_info.objects.get(name = request.POST['name'],user_name = User.objects.get(username=request.POST['username']))
    a.active = request.POST['active']
    a.save()
    reply = {
        "reply":str(cn)
    }
    return Response(reply)
# url = sms/get-unsend-msg/
# get 1 unsend msg
@api_view(['POST'])
def getunsendmsg(request):
    if sms.objects.filter(user_name = User.objects.get(username=request.POST['username']),sms_status='no').exists():
        get_sms = sms.objects.filter(user_name = User.objects.get(username=request.POST['username']),sms_status='no')[0]
        srl = get_unsend_msg(get_sms)
        return Response(srl.data)
    else:
        return Response({})
# url = sms/sms-status-save/
# save the status of a sms
@api_view(['POST'])
def save_status_sms(request):
    if sms.objects.filter(id = request.POST['sms_id'],user_name=User.objects.get(username=request.POST['username'])).exists():
        smsstatus = sms.objects.get(id = request.POST['sms_id'],user_name=User.objects.get(username=request.POST['username']))
        smsstatus.sms_status = 'yes'
        smsstatus.save()
        reqly = {
        'reqly':'success'
        }
    else:
        reqly = {
            'reqly':'error'
        }

    return Response(reqly)
# url = sms/get-suggetion-for-num/
# api for get number suggetion
@api_view(['POST'])
def get_suggetions(request):
    if request.POST['str'].isalpha():
        nm = phone_book.objects.filter(name__icontains = request.POST['str'],user_name=User.objects.get(username=request.POST['username']))
        srl = get_hint(nm,many=True)
        return Response(srl.data)
    else:
        nm = phone_book.objects.filter(number__icontains = request.POST['str'],user_name=User.objects.get(username=request.POST['username']))
        srl = get_hint(nm,many=True)
        return Response(srl.data)

# api for group number search for add
# url = sms/add-number-to-group-search/
@api_view(['POST'])
def get_group_search(request):
    if request.POST['str'].isalpha():
        group = group_name.objects.get(id=request.POST['group_id'])
        number = phone_book.objects.filter(~Q(group=group.id),phonebook = group.phonebook.id,name__icontains=request.POST['str']).order_by('name')
        srl = get_hint(number,many=True)
    else:
        group = group_name.objects.get(id=request.POST['group_id'])
        number = phone_book.objects.filter(~Q(group=group.id),phonebook = group.phonebook.id,number__icontains=request.POST['str']).order_by('name')
        srl = get_hint(number,many=True)
    return Response(srl.data)
# api to make device active and in active
# url = sms/make-active/
@api_view(['POST'])
def make_active(request):
    # if device_info.objects.get(name = request.POST['name'],user_name = User.objects.get(username=request.POST['username'])).exists():
    #     d = device_info.objects.get(name = request.POST['name'],user_name = User.objects.get(username=request.POST['username']))
    #     d.active = request.POST['active']
    #     d.save()
    # else:
    #     reply = {
    #         'reply':'error'
    #     }
    a = device_info.objects.filter(user_name = User.objects.get(username=request.POST['username']))
    for aa in a:
        aa.active = 'no'
        aa.save()
    reply = {
        'reply':"hi"
    }
    return Response(reply)
# url = sms/get-sms-from-phone/
# this api is to received sms from the phn
@api_view(['POST'])
def received_sms(request):
    dt = datetime.datetime.now()
    x = str(dt.day)+'-'+str(dt.month)+'-'+str(dt.year)
    msgs = json.loads(request.POST['message'])
    if not date_info.objects.filter(date=x,user_name=User.objects.get(username=request.POST['username'])).exists():
        dateinfo = date_info()
        dateinfo.user_name = User.objects.get(username = request.POST['username'])
        dateinfo.date = x
        dateinfo.save()
    for m in msgs:
        if phone_book.objects.filter(number__contains = m['address'],user_name = User.objects.get(username = request.POST['username']) ).exists():
            snd = sms()
            aaaaa = phone_book.objects.filter(number__contains= m['address'],user_name=User.objects.get(username = request.POST['username']) )[0]
            snd.phone = aaaaa
            snd.user_name = User.objects.get(username = request.POST['username'])
            snd.number = aaaaa.number
            snd.message = m['msg']
            snd.sms_status = 'received'
            snd.date = x
            snd.readornot = 'not'
            snd.save()
        else:
            snd = sms()
            snd.user_name = User.objects.get(username = request.POST['username'])
            snd.number = m['address']
            snd.message = m['msg']
            snd.sms_status = 'received'
            snd.date = x
            snd.readornot = 'not'
            snd.save()
        reply = {
            'reply':"hi"
        }
        return Response(reply)
