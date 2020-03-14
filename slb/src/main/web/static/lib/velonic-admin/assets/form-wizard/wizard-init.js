/**
* Theme: Velonic Admin Template
* Author: Coderthemes
* Form wizard page
*/

!function($) {
    "use strict";

    var FormWizard = function() {};

    FormWizard.prototype.createBasic = function($form_container) {
        $form_container.children("div").steps({
            headerTag: "h3",
            bodyTag: "section",
            transitionEffect: "slideLeft"
        });
        return $form_container;
    },
    //creates form with validation
    FormWizard.prototype.createValidatorForm = function($form_container) {
        $form_container.validate({
            errorPlacement: function errorPlacement(error, element) {
                element.after(error);
            }
        });
        $form_container.children("div").steps({
            headerTag: "h3",
            bodyTag: "section",
            transitionEffect: "slideLeft",
            onStepChanging: function (event, currentIndex, newIndex) {
                $form_container.validate().settings.ignore = ":disabled,:hidden";
                return $form_container.valid();
            },
            onFinishing: function (event, currentIndex) {
                $('#failed-div').text('');
                $('#success-div').text('');
                var bodyText=$('#postbody').text();
                var urlText=$('#posturl').text();
                var postData=JSON.parse(bodyText);
                postData.name=$('#policyname').val();
                $.each(postData['policy-virtual-servers'], function (index, item) {
                    item.priority=parseInt($('#priority').val());
                });

                $form_container.validate().settings.ignore = ":disabled";
                if($form_container.valid()){
                    $.ajax({
                        xhrFields: {
                            withCredentials: true
                        },
                        url: urlText.trim(),
                        data: JSON.stringify(postData),
                        type: "POST",
                        dataType: "json",
                        timeout: 99000,
                        beforeSend: function () {
                        },
                        error: function (xhr, status, error) {
                            $('#confirmAddTrafficPolicyResult').modal('show').find('#failed-div').text('创建traffic policy 失败， 失败原因'+xhr.responseText);
                            return false;
                        },
                        success: function (result) {
                            $('#confirmAddTrafficPolicyResult').modal('show').find('#success-div').text('创建traffic policy 成功, 2s 后跳转到Traffic Policy 页面');

                            var env = $('#env').text();
                            setTimeout(function () {
                                window.location.href='/portal/policy#?env='+env;
                            },2000);

                            return true;
                        },
                        complete: function () {
                        }
                    });
                }else{
                    return false;
                }
            },
            onFinished: function (event, currentIndex) {
                alert("Submitted!");
            }
        });

        return $form_container;
    },
    //creates vertical form
    FormWizard.prototype.createVertical = function($form_container) {
        $form_container.steps({
            headerTag: "h3",
            bodyTag: "section",
            transitionEffect: "fade",
            stepsOrientation: "vertical"
        });
        return $form_container;
    },
    FormWizard.prototype.init = function() {
        //initialzing various forms

        //basic form
        this.createBasic($("#basic-form"));

        //form with validation
        this.createValidatorForm($("#wizard-validation-form"));

        //vertical form
        this.createVertical($("#wizard-vertical"));
    },
    //init
    $.FormWizard = new FormWizard, $.FormWizard.Constructor = FormWizard
}(window.jQuery),

//initializing 
function($) {
    "use strict";
    $.FormWizard.init()
}(window.jQuery);