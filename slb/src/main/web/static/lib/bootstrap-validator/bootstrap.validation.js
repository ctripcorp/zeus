/* =========================================================
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ========================================================= */

!function () {
    var BootstrapValidation = function (el, options) {
        this.options = options;
        this.$el = $(el);
        this.$div = $('<div class="validation-group"></div>');
//    this.$div.width(this.$el.css('width'));
        this.$result = null;
        this.$timeoutId = 0;

        this.init();
    }

    BootstrapValidation.LOCALES = [];

    BootstrapValidation.LOCALES['en-US'] = {
        requiredMessageFormatter: function () {
            return 'This is required!';
        },
        integerMessageFormatter: function () {
            return 'Integer only!';
        },
        numberMessageFormatter: function () {
            return 'Number only!';
        },
        ipMessageFormatter: function () {
            return 'Invalid IP address!';
        },
        emailMessageFormatter: function () {
            return 'Invalid email address!';
        },
        charMessageFormatter: function () {
            return 'Character only!';
        },
        urlMessageFormatter: function () {
            return 'Invalid URL address!';
        },
        dateMessageFormatter: function () {
            return "Invalid data format (yyyy-MM-dd)!";
        }
    }

    BootstrapValidation.LOCALES['zh-CN'] = {
        requiredMessageFormatter: function () {
            return 'Required Field！';
        },
        integerMessageFormatter: function () {
            return 'Please Check Number Format！';
        },
        numberMessageFormatter: function () {
            return 'Please Check Number Format！';
        },
        ipMessageFormatter: function () {
            return 'Please Check IP Address Format！';
        },
        emailMessageFormatter: function () {
            return 'Please Check Mail Address Format！';
        },
        charMessageFormatter: function () {
            return 'Please Input Characters！';
        },
        urlMessageFormatter: function () {
            return 'Please Input Correct Website Address！';
        },
        dateMessageFormatter: function () {
            return "Required Format: yyyy-MM-dd！";
        }
    }

    BootstrapValidation.DEFAULTS = {
        validationTimeOut: 500,
        placement: 'right',
        rules: {
            'required': function (value) {
                return $.trim(value) != '' ? '' : BootstrapValidation.DEFAULTS['requiredMessageFormatter']();
            },
            'integer': function (value) {
                return ($.trim(value) == '' || /^[0-9]\d*$/.test(value)) ? '' : BootstrapValidation.DEFAULTS['integerMessageFormatter']();
            },
            'number': function (value) {
                return ($.trim(value) == '' || /^-?(?:\d+|\d{1,3}(?:,\d{3})+)?(?:\.\d+)?$/.test(value)) ? '' : BootstrapValidation.DEFAULTS['numberMessageFormatter']();
            },
            'ip': function (value) {
                return ($.trim(value) == '' || /^(25[0-5]|2[0-4][0-9]|1[0-9]{2}|[1-9][0-9]|[1-9])(\.(25[0-5]|2[0-4][0-9]|1?[0-9]{1,2})){2}\.(25[0-5]|2[0-4][0-9]|1[0-9]{2}|[1-9][0-9]|[0-9])$/.test(value)) || /^\s*((([0-9A-Fa-f]{1,4}:){7}([0-9A-Fa-f]{1,4}|:))|(([0-9A-Fa-f]{1,4}:){6}(:[0-9A-Fa-f]{1,4}|((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3})|:))|(([0-9A-Fa-f]{1,4}:){5}(((:[0-9A-Fa-f]{1,4}){1,2})|:((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3})|:))|(([0-9A-Fa-f]{1,4}:){4}(((:[0-9A-Fa-f]{1,4}){1,3})|((:[0-9A-Fa-f]{1,4})?:((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){3}(((:[0-9A-Fa-f]{1,4}){1,4})|((:[0-9A-Fa-f]{1,4}){0,2}:((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){2}(((:[0-9A-Fa-f]{1,4}){1,5})|((:[0-9A-Fa-f]{1,4}){0,3}:((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){1}(((:[0-9A-Fa-f]{1,4}){1,6})|((:[0-9A-Fa-f]{1,4}){0,4}:((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3}))|:))|(:(((:[0-9A-Fa-f]{1,4}){1,7})|((:[0-9A-Fa-f]{1,4}){0,5}:((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3}))|:)))(%.+)?\s*$/.test(value) ? '' : BootstrapValidation.DEFAULTS['ipMessageFormatter']();
            },

            'email': function (value) {
                return ($.trim(value) == '' || /^((([a-z]|\d|[!#\$%&'\*\+\-\/=\?\^_`{\|}~]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])+(\.([a-z]|\d|[!#\$%&'\*\+\-\/=\?\^_`{\|}~]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])+)*)|((\x22)((((\x20|\x09)*(\x0d\x0a))?(\x20|\x09)+)?(([\x01-\x08\x0b\x0c\x0e-\x1f\x7f]|\x21|[\x23-\x5b]|[\x5d-\x7e]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(\\([\x01-\x09\x0b\x0c\x0d-\x7f]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF]))))*(((\x20|\x09)*(\x0d\x0a))?(\x20|\x09)+)?(\x22)))@((([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])*([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])))\.)+(([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])*([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])))$/i.test(value)) ? '' : BootstrapValidation.DEFAULTS['emailMessageFormatter']();
            },
            'char': function (value) {
                return ($.trim(value) == '' || /^[A-Za-z0-9_]*$/.test(value)) ? '' : BootstrapValidation.DEFAULTS['charMessageFormatter']();
            },
            'url': function (value) {
                return ($.trim(value) == '' || /^(https?|s?ftp):\/\/(((([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(%[\da-f]{2})|[!\$&'\(\)\*\+,;=]|:)*@)?(((\d|[1-9]\d|1\d\d|2[0-4]\d|25[0-5])\.(\d|[1-9]\d|1\d\d|2[0-4]\d|25[0-5])\.(\d|[1-9]\d|1\d\d|2[0-4]\d|25[0-5])\.(\d|[1-9]\d|1\d\d|2[0-4]\d|25[0-5]))|((([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])*([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])))\.)+(([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])*([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])))\.?)(:\d*)?)(\/((([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(%[\da-f]{2})|[!\$&'\(\)\*\+,;=]|:|@)+(\/(([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(%[\da-f]{2})|[!\$&'\(\)\*\+,;=]|:|@)*)*)?)?(\?((([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(%[\da-f]{2})|[!\$&'\(\)\*\+,;=]|:|@)|[\uE000-\uF8FF]|\/|\?)*)?(#((([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(%[\da-f]{2})|[!\$&'\(\)\*\+,;=]|:|@)|\/|\?)*)?$/i.test(value)) ? '' : BootstrapValidation.DEFAULTS['urlMessageFormatter']();
            },
            'date': function (value) {
                return ($.trim(value) == '' || /Invalid|NaN/.test(new Date(value).toString())) ? '' : BootstrapValidation.DEFAULTS['dateMessageFormatter']();
            }
        }
    };

    $.extend(BootstrapValidation.DEFAULTS, BootstrapValidation.LOCALES['zh-CN']);

    BootstrapValidation.prototype.init = function () {
        this.$el.after(this.$div);
        this.$div.append(this.$el);
        this.setValidator();
    };

    BootstrapValidation.prototype.reset = function (option) {
        this.options = $.extend({}, BootstrapValidation.DEFAULTS, this.$el.data(), typeof option === 'object' && option);
        var val = this.$el.val();
        if (this.options.hasOwnProperty('value')) {
            var value = this.options['value'];
            if (value !== undefined && value !== null || typeof value === 'string') {
                val = value;
            }
        }

        this.$el.val(val);
        var $span = this.$el.next();
        if ($span != undefined && $span != null) {
            $span.popover('hide');
        }
        this.$el.nextAll().remove();
        this.$el.removeClass('validation-success');
        this.$el.removeClass('validation-fail');
        this.$result = null;
    };

    BootstrapValidation.prototype.validate = function (showMessage) {
        if (this.$result == null) {
            this.validateRules();
        }

        if (showMessage == undefined || showMessage) {
            this.setResult(this.$result);
        }

        return this.$result.success;
    };

    BootstrapValidation.prototype.setValidator = function () {
        var $this = this;
        $this.$el.bind('input propertychange', function (event) {
            clearTimeout($this.$timeoutId);
            $this.$timeoutId = setTimeout(function () {
                $this.$result = null;
                $this.validate(true);
            }, $this.options.validationTimeOut);
        });
    }

    BootstrapValidation.prototype.validateRules = function () {
        var $this = this,
            value = $this.$el.val(),
            result = {'isValidated': false};

        var validator = $this.options['validator'];
        if (typeof validator !== 'string')
            return result;

        var ruleNames = validator.split(' ');
        if (ruleNames && ruleNames.length > 0) {
            $.each(ruleNames, function (index, ruleName) {
                var validator = $this.options['rules'][ruleName];
                if (validator === undefined || validator === null || typeof validator !== 'function') {
                    validator = window[ruleName];
                }
                if (validator === undefined || validator === null || typeof validator !== 'function') {
                    return true;
                }

                result.errorMsg = validator(value);
                result.success = result.errorMsg === '';
                result.isValidated = true;
                return result.success;
            });
        }

        if (result.isValidated) {
            $($this).trigger('validated.bs.input', [result.success, value]);
        }

        $this.$result = result;
        return result;
    };

    BootstrapValidation.prototype.setResult = function (result) {
        if (result) {
            var $span = this.$el.next();
            $span.popover('hide');
            this.$el.nextAll().remove();
            this.$el.removeClass('validation-success');
            this.$el.removeClass('validation-fail');

            if (!result.isValidated) {
                return true;
            }

            var iconClass, statusClass;
            if (result.success) {
                statusClass = 'validation-success';
                $span = $('<span class="glyphicon glyphicon-ok validation-feedback" aria-hidden="true"></span>');
            } else {
                statusClass = 'validation-fail';
                $span = $('<span class="glyphicon glyphicon-remove validation-feedback" aria-hidden="true" data-container="body" data-toggle="popover" data-placement="' + this.options.placement + '" data-content="' + result.errorMsg + '"></span>');
            }
            this.$el.addClass(statusClass);
            this.$el.after($span);
            if (!result.success) {
                $span.popover('show');
            }
            return result.success;
        } else {
            result = this.validateRules();
            return this.setResult(result);
        }
    };

    BootstrapValidation.prototype.setLocale = function (locale) {
        $.extend($.fn.bootstrapValidation.defaults, $.fn.bootstrapValidation.locales[locale]);
    }

    var allowedMethods = ['destroy', 'setLocale', 'reset', 'validate'];

    $.fn.bootstrapValidation = function (option) {
        var value,
            args = Array.prototype.slice.call(arguments, 1);

        this.each(function () {
            var $this = $(this),
                data = $this.data('bootstrap.validation'),
                options = $.extend({}, BootstrapValidation.DEFAULTS, $this.data(), typeof option === 'object' && option);

            if (typeof option === 'string') {
                if ($.inArray(option, allowedMethods) < 0) {
                    throw new Error("Unknown method: " + option);
                }

                if (!data) {
                    return;
                }

                value = data[option].apply(data, args);

                if (option === 'destroy') {
                    $this.removeData('bootstrap.validation');
                }
            }

            if (!data) {
                $this.data('bootstrap.validation', (data = new BootstrapValidation(this, options)));
            }
        });

        return typeof value === 'undefined' ? this : value;
    };

    $.fn.bootstrapValidation.Constructor = BootstrapValidation;
    $.fn.bootstrapValidation.defaults = BootstrapValidation.DEFAULTS;
    $.fn.bootstrapValidation.locales = BootstrapValidation.LOCALES;

    $(function () {
        $('[data-validator-type="validation"]').bootstrapValidation();
    });
}(jQuery);
