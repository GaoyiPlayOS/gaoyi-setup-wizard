# SUW for GaoyiPlayOS - Prebuilt
This branch features latest SUW prebuilt version for GaoyiPlayOS. For those who want to give it a try and don't want to compile it from scratch.

## Directory Structure
```bash
 /
└──  system
    └──  system_ext
        ├──  etc
        │   └──  permissions
        │       └── 󰗀 privapp-permissions-setupwizard.xml
        └──  priv-app
            └──  SetupWizard
                └──  SetupWizard.apk
```

Files are already in place. Put them on real environment.

## Permissions
Run the following command **as root**:
```bash
# SetupWizard itself
chmod 755 /system/system_ext/priv-app/SetupWizard
chmod 644 /system/system_ext/priv-app/SetupWizard/SetupWizard.apk
chown -R root:root /system/system_ext/priv-app/SetupWizard

# Permissions configuration
chmod 644 /system/system_ext/etc/permissions/privapp-permissions-setupwizard.xml
chown root:root /system/system_ext/etc/permissions/privapp-permissions-setupwizard.xml
```

## SELinux Context
Don't ignore them, as they're playing an important role on most of Android system.
```bash
# Use `restorecon`.
# It will automatically fix the SELinux Context by the system policy.

restorecon -R /system/system_ext/priv-app/SetupWizard
restorecon /system/system_ext/etc/permissions/privapp-permissions-setupwizard.xml
```

> [!caution]
> You **must** fix permissions and SELinux context. Otherwise the system will be resulted in bootloop!

## Put device into Provisioning Mode
To test this SUW, you must put your device into the "Provisioning Mode". Run the following command:
```bash
# Reset global provision status
settings put global device_provisioned 0

# Reset userland provision status
settings put secure user_setup_complete 0
settings put secure --user 0 user_setup_complete 0

# Some systems use a property to determine provision status.
# If exists, reset it.
#getprop DEVICE_PROVISIONED
setprop DEVICE_PROVISIONED 0
```

After resets, **reboot** your environment immediately.

## Q & A
### Q: Why the Package Installer reports "Signature Conflict"?
**A:** This SUW is especially designed for AOSP-based builds. It used `platform` certificate from Google and works on **most of AOSP-based systems** (LineageOS, RisingOS, etc.), not only GaoyiPlayOS.
However, if you're using a **heavy modified AOSP** system (like HyperOS), then this SUW **is not compatible** for you. - Since the OEM changed trustchain from Google's to themselves.
